package com.example.hex.engine

sealed class NexusExpr {
    data class Literal(val value: Any?) : NexusExpr()
    data class Identifier(val name: String, val line: Int, val col: Int) : NexusExpr()
    data class Binary(val op: String, val left: NexusExpr, val right: NexusExpr, val line: Int, val col: Int) : NexusExpr()
    data class Unary(val op: String, val right: NexusExpr, val line: Int, val col: Int) : NexusExpr()
    data class Call(val callee: NexusExpr, val args: List<NexusExpr>, val line: Int, val col: Int) : NexusExpr()
    data class ListLiteral(val elements: List<NexusExpr>) : NexusExpr()
    data class MapLiteral(val entries: List<Pair<NexusExpr, NexusExpr>>) : NexusExpr()
    data class IndexAccess(val target: NexusExpr, val index: NexusExpr, val line: Int, val col: Int) : NexusExpr()
    data class AttributeAccess(val target: NexusExpr, val attrName: String, val line: Int, val col: Int) : NexusExpr()
}

sealed class NexusStmt {
    data class VarDecl(val name: String, val init: NexusExpr, val isConst: Boolean, val line: Int, val col: Int) : NexusStmt()
    data class Assign(val target: NexusExpr, val value: NexusExpr, val line: Int, val col: Int) : NexusStmt()
    data class Print(val expr: NexusExpr, val line: Int, val col: Int) : NexusStmt()
    data class If(val condition: NexusExpr, val thenBranch: List<NexusStmt>, val elseBranch: List<NexusStmt>) : NexusStmt()
    data class While(val condition: NexusExpr, val body: List<NexusStmt>) : NexusStmt()
    data class ForRange(val varName: String, val startExpr: NexusExpr, val endExpr: NexusExpr, val body: List<NexusStmt>, val line: Int, val col: Int) : NexusStmt()
    data class ForEach(val varName: String, val iterable: NexusExpr, val body: List<NexusStmt>, val line: Int, val col: Int) : NexusStmt()
    data class Return(val expr: NexusExpr?, val line: Int, val col: Int) : NexusStmt()
    data class FuncDecl(val name: String, val params: List<String>, val body: List<NexusStmt>, val line: Int, val col: Int) : NexusStmt()
    data class ClassDecl(val name: String, val methods: List<FuncDecl>, val line: Int, val col: Int) : NexusStmt()
    data class TryCatch(val tryBody: List<NexusStmt>, val excVar: String, val catchBody: List<NexusStmt>) : NexusStmt()
    data class Throw(val expr: NexusExpr, val line: Int, val col: Int) : NexusStmt()
    data class Import(val modulePath: String, val line: Int, val col: Int) : NexusStmt()
    data class Break(val line: Int, val col: Int) : NexusStmt()
    data class Continue(val line: Int, val col: Int) : NexusStmt()
    data class ExprStmt(val expr: NexusExpr) : NexusStmt()
}
