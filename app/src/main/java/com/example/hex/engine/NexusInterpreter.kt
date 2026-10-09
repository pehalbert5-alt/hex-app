package com.example.hex.engine

import java.io.File

class ReturnSignal(val value: Any?) : Exception()
class BreakSignal : Exception()
class ContinueSignal : Exception()
class UserErrorSignal(msg: String) : Exception(msg)

class NexusEnvironment(val parent: NexusEnvironment? = null) {
    val vars = mutableMapOf<String, Any?>()
    val constants = mutableSetOf<String>()

    fun get(name: String): Any? {
        if (vars.containsKey(name)) return vars[name]
        return parent?.get(name) ?: throw RuntimeException("La variable '$name' n'existe pas.")
    }

    fun set(name: String, value: Any?) {
        var env: NexusEnvironment? = this
        while (env != null) {
            if (env.vars.containsKey(name)) {
                if (env.constants.contains(name)) {
                    throw RuntimeException("'$name' est une constante et ne peut pas être modifiée.")
                }
                env.vars[name] = value
                return
            }
            env = env.parent
        }
        if (constants.contains(name)) {
            throw RuntimeException("'$name' est une constante et ne peut pas être modifiée.")
        }
        vars[name] = value
    }

    fun define(name: String, value: Any?, isConst: Boolean = false) {
        if (constants.contains(name)) {
            throw RuntimeException("'$name' est une constante et ne peut pas être modifiée.")
        }
        vars[name] = value
        if (isConst) {
            constants.add(name)
        }
    }
}

data class NexusFunction(
    val name: String,
    val params: List<String>,
    val body: List<NexusStmt>,
    val closure: NexusEnvironment
)

data class NexusClass(
    val name: String,
    val methods: Map<String, NexusFunction>
)

data class NexusInstance(
    val klass: NexusClass,
    val fields: MutableMap<String, Any?> = mutableMapOf()
)

class NexusInterpreter(
    val output: (String) -> Unit,
    val inputPrompt: (String) -> String,
    val workspaceDir: File
) {

    val globalEnv = NexusEnvironment()
    private val importedModules = mutableSetOf<String>()

    init {
        val builtins = NexusBuiltins.getBuiltins(output, inputPrompt, workspaceDir)
        builtins.forEach { (name, fn) ->
            globalEnv.define(name, fn)
        }
    }

    fun executeProgram(program: List<NexusStmt>, env: NexusEnvironment = globalEnv) {
        for (stmt in program) {
            executeStmt(stmt, env)
        }
    }

    private fun executeStmt(stmt: NexusStmt, env: NexusEnvironment) {
        when (stmt) {
            is NexusStmt.VarDecl -> {
                val valRes = evalExpr(stmt.init, env)
                env.define(stmt.name, valRes, stmt.isConst)
            }
            is NexusStmt.Assign -> {
                when (val target = stmt.target) {
                    is NexusExpr.Identifier -> {
                        val valRes = evalExpr(stmt.value, env)
                        env.set(target.name, valRes)
                    }
                    is NexusExpr.IndexAccess -> {
                        val obj = evalExpr(target.target, env)
                        val idx = evalExpr(target.index, env)
                        val valRes = evalExpr(stmt.value, env)
                        if (obj is MutableList<*>) {
                            val list = obj as MutableList<Any?>
                            val i = (idx as Number).toInt()
                            if (i in 0 until list.size) list[i] = valRes
                        } else if (obj is MutableMap<*, *>) {
                            val map = obj as MutableMap<Any?, Any?>
                            map[idx] = valRes
                        }
                    }
                    is NexusExpr.AttributeAccess -> {
                        val obj = evalExpr(target.target, env)
                        val valRes = evalExpr(stmt.value, env)
                        if (obj is NexusInstance) {
                            obj.fields[target.attrName] = valRes
                        } else if (obj is MutableMap<*, *>) {
                            val map = obj as MutableMap<Any?, Any?>
                            map[target.attrName] = valRes
                        }
                    }
                    else -> throw RuntimeException("Cible d'affectation invalide.")
                }
            }
            is NexusStmt.Print -> {
                val v = evalExpr(stmt.expr, env)
                output(NexusBuiltins.formatValue(v))
            }
            is NexusStmt.If -> {
                val cond = isTruthy(evalExpr(stmt.condition, env))
                if (cond) {
                    executeBlock(stmt.thenBranch, NexusEnvironment(env))
                } else {
                    executeBlock(stmt.elseBranch, NexusEnvironment(env))
                }
            }
            is NexusStmt.While -> {
                while (isTruthy(evalExpr(stmt.condition, env))) {
                    try {
                        executeBlock(stmt.body, NexusEnvironment(env))
                    } catch (_: BreakSignal) {
                        break
                    } catch (_: ContinueSignal) {
                        continue
                    }
                }
            }
            is NexusStmt.ForRange -> {
                val start = (evalExpr(stmt.startExpr, env) as Number).toLong()
                val end = (evalExpr(stmt.endExpr, env) as Number).toLong()
                for (i in start..end) {
                    val loopEnv = NexusEnvironment(env)
                    loopEnv.define(stmt.varName, i)
                    try {
                        executeBlock(stmt.body, loopEnv)
                    } catch (_: BreakSignal) {
                        break
                    } catch (_: ContinueSignal) {
                        continue
                    }
                }
            }
            is NexusStmt.ForEach -> {
                val iter = evalExpr(stmt.iterable, env)
                val list = when (iter) {
                    is List<*> -> iter
                    is Map<*, *> -> iter.keys.toList()
                    is String -> iter.map { it.toString() }
                    else -> emptyList<Any?>()
                }
                for (item in list) {
                    val loopEnv = NexusEnvironment(env)
                    loopEnv.define(stmt.varName, item)
                    try {
                        executeBlock(stmt.body, loopEnv)
                    } catch (_: BreakSignal) {
                        break
                    } catch (_: ContinueSignal) {
                        continue
                    }
                }
            }
            is NexusStmt.FuncDecl -> {
                val fn = NexusFunction(stmt.name, stmt.params, stmt.body, env)
                env.define(stmt.name, fn)
            }
            is NexusStmt.ClassDecl -> {
                val methodsMap = mutableMapOf<String, NexusFunction>()
                for (m in stmt.methods) {
                    methodsMap[m.name] = NexusFunction(m.name, m.params, m.body, env)
                }
                val klass = NexusClass(stmt.name, methodsMap)
                env.define(stmt.name, klass)
            }
            is NexusStmt.Return -> {
                val v = stmt.expr?.let { evalExpr(it, env) }
                throw ReturnSignal(v)
            }
            is NexusStmt.Break -> throw BreakSignal()
            is NexusStmt.Continue -> throw ContinueSignal()
            is NexusStmt.TryCatch -> {
                try {
                    executeBlock(stmt.tryBody, NexusEnvironment(env))
                } catch (e: Exception) {
                    val catchEnv = NexusEnvironment(env)
                    val msg = if (e is UserErrorSignal) e.message else e.message ?: "Erreur"
                    catchEnv.define(stmt.excVar, msg)
                    executeBlock(stmt.catchBody, catchEnv)
                }
            }
            is NexusStmt.Throw -> {
                val v = evalExpr(stmt.expr, env)
                throw UserErrorSignal(NexusBuiltins.formatValue(v))
            }
            is NexusStmt.Import -> {
                val path = stmt.modulePath
                if (!importedModules.contains(path)) {
                    importedModules.add(path)
                    val file = File(workspaceDir, if (path.endsWith(".nx")) path else "$path.nx")
                    if (file.exists()) {
                        val source = file.readText()
                        val tokens = NexusLexer(source).tokenize()
                        val prog = NexusParser(tokens).parseProgram()
                        executeProgram(prog, env)
                    } else {
                        output("Module introuvable: $path")
                    }
                }
            }
            is NexusStmt.ExprStmt -> {
                evalExpr(stmt.expr, env)
            }
        }
    }

    private fun executeBlock(stmts: List<NexusStmt>, env: NexusEnvironment) {
        for (s in stmts) {
            executeStmt(s, env)
        }
    }

    private fun evalExpr(expr: NexusExpr, env: NexusEnvironment): Any? {
        return when (expr) {
            is NexusExpr.Literal -> expr.value
            is NexusExpr.Identifier -> env.get(expr.name)
            is NexusExpr.Unary -> {
                val v = evalExpr(expr.right, env)
                when (expr.op) {
                    "non" -> !isTruthy(v)
                    "-" -> {
                        when (v) {
                            is Long -> -v
                            is Double -> -v
                            is Int -> -v
                            else -> 0L
                        }
                    }
                    else -> v
                }
            }
            is NexusExpr.Binary -> {
                val left = evalExpr(expr.left, env)
                val right = evalExpr(expr.right, env)
                when (expr.op) {
                    "+" -> {
                        if (left is String || right is String) {
                            NexusBuiltins.formatValue(left) + NexusBuiltins.formatValue(right)
                        } else if (left is Double || right is Double) {
                            (left as Number).toDouble() + (right as Number).toDouble()
                        } else {
                            (left as Number).toLong() + (right as Number).toLong()
                        }
                    }
                    "-" -> {
                        if (left is Double || right is Double) {
                            (left as Number).toDouble() - (right as Number).toDouble()
                        } else {
                            (left as Number).toLong() - (right as Number).toLong()
                        }
                    }
                    "*" -> {
                        if (left is Double || right is Double) {
                            (left as Number).toDouble() * (right as Number).toDouble()
                        } else {
                            (left as Number).toLong() * (right as Number).toLong()
                        }
                    }
                    "/" -> {
                        val r = (right as Number).toDouble()
                        if (r == 0.0) throw RuntimeException("Division par zéro.")
                        (left as Number).toDouble() / r
                    }
                    "%" -> {
                        (left as Number).toLong() % (right as Number).toLong()
                    }
                    "==" -> left == right
                    "!=" -> left != right
                    "<" -> (left as Number).toDouble() < (right as Number).toDouble()
                    ">" -> (left as Number).toDouble() > (right as Number).toDouble()
                    "<=" -> (left as Number).toDouble() <= (right as Number).toDouble()
                    ">=" -> (left as Number).toDouble() >= (right as Number).toDouble()
                    "et" -> isTruthy(left) && isTruthy(right)
                    "ou" -> isTruthy(left) || isTruthy(right)
                    else -> null
                }
            }
            is NexusExpr.Call -> {
                val callee = evalExpr(expr.callee, env)
                val args = expr.args.map { evalExpr(it, env) }

                when (callee) {
                    is Function1<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        (callee as (List<Any?>) -> Any?).invoke(args)
                    }
                    is NexusFunction -> {
                        val fnEnv = NexusEnvironment(callee.closure)
                        for (i in callee.params.indices) {
                            val pName = callee.params[i]
                            val argVal = if (i < args.size) args[i] else null
                            fnEnv.define(pName, argVal)
                        }
                        try {
                            executeBlock(callee.body, fnEnv)
                            null
                        } catch (ret: ReturnSignal) {
                            ret.value
                        }
                    }
                    is NexusClass -> {
                        val instance = NexusInstance(callee)
                        val initMethod = callee.methods["initialiser"]
                        if (initMethod != null) {
                            val initEnv = NexusEnvironment(initMethod.closure)
                            initEnv.define("soi", instance)
                            for (i in initMethod.params.indices) {
                                val pName = initMethod.params[i]
                                val argVal = if (i < args.size) args[i] else null
                                initEnv.define(pName, argVal)
                            }
                            try {
                                executeBlock(initMethod.body, initEnv)
                            } catch (_: ReturnSignal) {}
                        }
                        instance
                    }
                    else -> throw RuntimeException("L'élément 'callee' n'est pas une fonction.")
                }
            }
            is NexusExpr.ListLiteral -> {
                expr.elements.map { evalExpr(it, env) }.toMutableList()
            }
            is NexusExpr.MapLiteral -> {
                val map = mutableMapOf<Any?, Any?>()
                expr.entries.forEach { (k, v) ->
                    map[evalExpr(k, env)] = evalExpr(v, env)
                }
                map
            }
            is NexusExpr.IndexAccess -> {
                val target = evalExpr(expr.target, env)
                val idx = evalExpr(expr.index, env)
                when (target) {
                    is List<*> -> {
                        val i = (idx as Number).toInt()
                        if (i in 0 until target.size) target[i] else null
                    }
                    is Map<*, *> -> target[idx]
                    is String -> {
                        val i = (idx as Number).toInt()
                        if (i in 0 until target.length) target[i].toString() else ""
                    }
                    else -> null
                }
            }
            is NexusExpr.AttributeAccess -> {
                val target = evalExpr(expr.target, env)
                when (target) {
                    is NexusInstance -> {
                        if (target.fields.containsKey(expr.attrName)) {
                            target.fields[expr.attrName]
                        } else if (target.klass.methods.containsKey(expr.attrName)) {
                            val method = target.klass.methods[expr.attrName]!!
                            // Bind "soi" to instance
                            { args: List<Any?> ->
                                val mEnv = NexusEnvironment(method.closure)
                                mEnv.define("soi", target)
                                for (i in method.params.indices) {
                                    mEnv.define(method.params[i], if (i < args.size) args[i] else null)
                                }
                                try {
                                    executeBlock(method.body, mEnv)
                                    null
                                } catch (ret: ReturnSignal) {
                                    ret.value
                                }
                            }
                        } else {
                            null
                        }
                    }
                    is Map<*, *> -> target[expr.attrName]
                    else -> null
                }
            }
        }
    }

    private fun isTruthy(v: Any?): Boolean {
        return when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            is List<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
    }
}
