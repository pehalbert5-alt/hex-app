package com.example.hex.engine

class NexusParser(private val tokens: List<Token>) {

    private var current = 0

    fun parseProgram(): List<NexusStmt> {
        val stmts = mutableListOf<NexusStmt>()
        while (!isAtEnd()) {
            skipNewlines()
            if (isAtEnd()) break
            stmts.add(parseStatement())
        }
        return stmts
    }

    private fun skipNewlines() {
        while (match(TokenType.NL)) {
            // consume
        }
    }

    private fun parseStatement(): NexusStmt {
        skipNewlines()
        val tok = peek()

        return when (tok.type) {
            TokenType.VARIABLE -> parseVarDecl(isConst = false)
            TokenType.CONSTANTE -> parseVarDecl(isConst = true)
            TokenType.AFFICHER -> parsePrint()
            TokenType.SI -> parseIf()
            TokenType.TANT_QUE -> parseWhile()
            TokenType.POUR -> parseFor()
            TokenType.FONCTION -> parseFuncDecl()
            TokenType.CLASSE -> parseClassDecl()
            TokenType.RETOURNER -> parseReturn()
            TokenType.ESSAYER -> parseTryCatch()
            TokenType.LANCER -> parseThrow()
            TokenType.IMPORTER -> parseImport()
            TokenType.ARRETER -> {
                advance()
                NexusStmt.Break(tok.line, tok.column)
            }
            TokenType.CONTINUER -> {
                advance()
                NexusStmt.Continue(tok.line, tok.column)
            }
            else -> parseExprOrAssign()
        }
    }

    private fun parseVarDecl(isConst: Boolean): NexusStmt {
        val declTok = advance() // VARIABLE or CONSTANTE
        val nameTok = consume(TokenType.IDENT, "Nom de variable attendu.")
        consume(TokenType.EGAL, "=' attendu après le nom de variable.")
        val initExpr = parseExpression()
        return NexusStmt.VarDecl(nameTok.value as String, initExpr, isConst, declTok.line, declTok.column)
    }

    private fun parsePrint(): NexusStmt {
        val printTok = advance() // AFFICHER
        val expr = parseExpression()
        return NexusStmt.Print(expr, printTok.line, printTok.column)
    }

    private fun parseIf(): NexusStmt {
        advance() // SI
        val cond = parseExpression()
        match(TokenType.ALORS)
        skipNewlines()

        val thenBranch = mutableListOf<NexusStmt>()
        val elseBranch = mutableListOf<NexusStmt>()

        while (!check(TokenType.FIN) && !check(TokenType.SINON) && !isAtEnd()) {
            thenBranch.add(parseStatement())
            skipNewlines()
        }

        if (match(TokenType.SINON)) {
            if (check(TokenType.SI)) {
                elseBranch.add(parseIf())
            } else {
                skipNewlines()
                while (!check(TokenType.FIN) && !isAtEnd()) {
                    elseBranch.add(parseStatement())
                    skipNewlines()
                }
                consume(TokenType.FIN, "'fin' attendu pour fermer 'sinon'.")
            }
        } else {
            consume(TokenType.FIN, "'fin' attendu pour fermer 'si'.")
        }

        return NexusStmt.If(cond, thenBranch, elseBranch)
    }

    private fun parseWhile(): NexusStmt {
        advance() // TANT_QUE
        val cond = parseExpression()
        match(TokenType.FAIRE)
        skipNewlines()

        val body = mutableListOf<NexusStmt>()
        while (!check(TokenType.FIN) && !isAtEnd()) {
            body.add(parseStatement())
            skipNewlines()
        }
        consume(TokenType.FIN, "'fin' attendu pour 'tant_que'.")

        return NexusStmt.While(cond, body)
    }

    private fun parseFor(): NexusStmt {
        val forTok = advance() // POUR
        if (match(TokenType.CHAQUE)) {
            val varTok = consume(TokenType.IDENT, "Nom de variable attendu après 'pour chaque'.")
            consume(TokenType.DANS, "'dans' attendu dans la boucle 'pour chaque'.")
            val iterExpr = parseExpression()
            match(TokenType.FAIRE)
            skipNewlines()

            val body = mutableListOf<NexusStmt>()
            while (!check(TokenType.FIN) && !isAtEnd()) {
                body.add(parseStatement())
                skipNewlines()
            }
            consume(TokenType.FIN, "'fin' attendu pour 'pour chaque'.")

            return NexusStmt.ForEach(varTok.value as String, iterExpr, body, forTok.line, forTok.column)
        } else {
            val varTok = consume(TokenType.IDENT, "Nom de variable attendu après 'pour'.")
            consume(TokenType.DE, "'de' attendu après le nom de variable.")
            val startExpr = parseExpression()
            consume(TokenType.JUSQUA, "'jusqu_a' attendu dans l'intervalle.")
            val endExpr = parseExpression()
            match(TokenType.FAIRE)
            skipNewlines()

            val body = mutableListOf<NexusStmt>()
            while (!check(TokenType.FIN) && !isAtEnd()) {
                body.add(parseStatement())
                skipNewlines()
            }
            consume(TokenType.FIN, "'fin' attendu pour 'pour'.")

            return NexusStmt.ForRange(varTok.value as String, startExpr, endExpr, body, forTok.line, forTok.column)
        }
    }

    private fun parseFuncDecl(): NexusStmt {
        val funcTok = advance() // FONCTION
        val nameTok = consume(TokenType.IDENT, "Nom de fonction attendu.")
        consume(TokenType.PAR_OUV, "'(' attendu après le nom de fonction.")

        val params = mutableListOf<String>()
        if (!check(TokenType.PAR_FER)) {
            do {
                val p = consume(TokenType.IDENT, "Nom de paramètre attendu.")
                params.add(p.value as String)
            } while (match(TokenType.VIRGULE))
        }
        consume(TokenType.PAR_FER, "')' attendu.")
        skipNewlines()

        val body = mutableListOf<NexusStmt>()
        while (!check(TokenType.FIN) && !isAtEnd()) {
            body.add(parseStatement())
            skipNewlines()
        }
        consume(TokenType.FIN, "'fin' attendu pour fermer la fonction.")

        return NexusStmt.FuncDecl(nameTok.value as String, params, body, funcTok.line, funcTok.column)
    }

    private fun parseClassDecl(): NexusStmt {
        val classTok = advance() // CLASSE
        val nameTok = consume(TokenType.IDENT, "Nom de classe attendu.")
        skipNewlines()

        val methods = mutableListOf<NexusStmt.FuncDecl>()
        while (!check(TokenType.FIN) && !isAtEnd()) {
            skipNewlines()
            if (check(TokenType.FONCTION)) {
                methods.add(parseFuncDecl() as NexusStmt.FuncDecl)
            } else {
                advance()
            }
            skipNewlines()
        }
        consume(TokenType.FIN, "'fin' attendu pour la classe.")

        return NexusStmt.ClassDecl(nameTok.value as String, methods, classTok.line, classTok.column)
    }

    private fun parseReturn(): NexusStmt {
        val retTok = advance() // RETOURNER
        var expr: NexusExpr? = null
        if (!check(TokenType.NL) && !check(TokenType.FIN) && !isAtEnd()) {
            expr = parseExpression()
        }
        return NexusStmt.Return(expr, retTok.line, retTok.column)
    }

    private fun parseTryCatch(): NexusStmt {
        advance() // ESSAYER
        skipNewlines()

        val tryBody = mutableListOf<NexusStmt>()
        while (!check(TokenType.ATTRAPER) && !check(TokenType.FIN) && !isAtEnd()) {
            tryBody.add(parseStatement())
            skipNewlines()
        }

        var excVar = "e"
        val catchBody = mutableListOf<NexusStmt>()

        if (match(TokenType.ATTRAPER)) {
            if (check(TokenType.IDENT)) {
                excVar = advance().value as String
            }
            skipNewlines()
            while (!check(TokenType.FIN) && !isAtEnd()) {
                catchBody.add(parseStatement())
                skipNewlines()
            }
            consume(TokenType.FIN, "'fin' attendu pour 'essayer'.")
        } else {
            consume(TokenType.FIN, "'fin' attendu pour 'essayer'.")
        }

        return NexusStmt.TryCatch(tryBody, excVar, catchBody)
    }

    private fun parseThrow(): NexusStmt {
        val throwTok = advance() // LANCER
        val expr = parseExpression()
        return NexusStmt.Throw(expr, throwTok.line, throwTok.column)
    }

    private fun parseImport(): NexusStmt {
        val impTok = advance() // IMPORTER
        val pathTok = consume(TokenType.TEXTE, "Nom de module sous forme de texte attendu.")
        return NexusStmt.Import(pathTok.value as String, impTok.line, impTok.column)
    }

    private fun parseExprOrAssign(): NexusStmt {
        val expr = parseExpression()
        if (match(TokenType.EGAL)) {
            val line = previous().line
            val col = previous().column
            val valueExpr = parseExpression()
            return NexusStmt.Assign(expr, valueExpr, line, col)
        }
        return NexusStmt.ExprStmt(expr)
    }

    private fun parseExpression(): NexusExpr {
        return parseLogicalOr()
    }

    private fun parseLogicalOr(): NexusExpr {
        var expr = parseLogicalAnd()
        while (match(TokenType.OU)) {
            val tok = previous()
            val right = parseLogicalAnd()
            expr = NexusExpr.Binary("ou", expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseLogicalAnd(): NexusExpr {
        var expr = parseEquality()
        while (match(TokenType.ET)) {
            val tok = previous()
            val right = parseEquality()
            expr = NexusExpr.Binary("et", expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseEquality(): NexusExpr {
        var expr = parseComparison()
        while (match(TokenType.OP_EGAL_EGAL, TokenType.OP_DIFF)) {
            val tok = previous()
            val op = if (tok.type == TokenType.OP_EGAL_EGAL) "==" else "!="
            val right = parseComparison()
            expr = NexusExpr.Binary(op, expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseComparison(): NexusExpr {
        var expr = parseAdditive()
        while (match(TokenType.OP_INF, TokenType.OP_SUP, TokenType.OP_INF_EGAL, TokenType.OP_SUP_EGAL)) {
            val tok = previous()
            val op = when (tok.type) {
                TokenType.OP_INF -> "<"
                TokenType.OP_SUP -> ">"
                TokenType.OP_INF_EGAL -> "<="
                TokenType.OP_SUP_EGAL -> ">="
                else -> ""
            }
            val right = parseAdditive()
            expr = NexusExpr.Binary(op, expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseAdditive(): NexusExpr {
        var expr = parseMultiplicative()
        while (match(TokenType.OP_PLUS, TokenType.OP_MOINS)) {
            val tok = previous()
            val op = if (tok.type == TokenType.OP_PLUS) "+" else "-"
            val right = parseMultiplicative()
            expr = NexusExpr.Binary(op, expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseMultiplicative(): NexusExpr {
        var expr = parseUnary()
        while (match(TokenType.OP_FOIS, TokenType.OP_DIV, TokenType.OP_MOD)) {
            val tok = previous()
            val op = when (tok.type) {
                TokenType.OP_FOIS -> "*"
                TokenType.OP_DIV -> "/"
                else -> "%"
            }
            val right = parseUnary()
            expr = NexusExpr.Binary(op, expr, right, tok.line, tok.column)
        }
        return expr
    }

    private fun parseUnary(): NexusExpr {
        if (match(TokenType.NON, TokenType.OP_MOINS)) {
            val tok = previous()
            val op = if (tok.type == TokenType.NON) "non" else "-"
            val right = parseUnary()
            return NexusExpr.Unary(op, right, tok.line, tok.column)
        }
        return parseCallOrAccess()
    }

    private fun parseCallOrAccess(): NexusExpr {
        var expr = parsePrimary()

        while (true) {
            if (match(TokenType.PAR_OUV)) {
                val tok = previous()
                val args = mutableListOf<NexusExpr>()
                if (!check(TokenType.PAR_FER)) {
                    do {
                        args.add(parseExpression())
                    } while (match(TokenType.VIRGULE))
                }
                consume(TokenType.PAR_FER, "')' attendu.")
                expr = NexusExpr.Call(expr, args, tok.line, tok.column)
            } else if (match(TokenType.CROCHET_OUV)) {
                val tok = previous()
                val indexExpr = parseExpression()
                consume(TokenType.CROCHET_FER, "']' attendu.")
                expr = NexusExpr.IndexAccess(expr, indexExpr, tok.line, tok.column)
            } else if (match(TokenType.POINT)) {
                val tok = previous()
                val attrTok = consume(TokenType.IDENT, "Nom d'attribut attendu après '.'.")
                expr = NexusExpr.AttributeAccess(expr, attrTok.value as String, tok.line, tok.column)
            } else {
                break
            }
        }

        return expr
    }

    private fun parsePrimary(): NexusExpr {
        val tok = peek()

        return when (tok.type) {
            TokenType.VRAI -> { advance(); NexusExpr.Literal(true) }
            TokenType.FAUX -> { advance(); NexusExpr.Literal(false) }
            TokenType.RIEN -> { advance(); NexusExpr.Literal(null) }
            TokenType.NOMBRE -> { advance(); NexusExpr.Literal(tok.value) }
            TokenType.TEXTE -> { advance(); NexusExpr.Literal(tok.value) }
            TokenType.IDENT -> { advance(); NexusExpr.Identifier(tok.value as String, tok.line, tok.column) }
            TokenType.PAR_OUV -> {
                advance()
                val expr = parseExpression()
                consume(TokenType.PAR_FER, "')' attendu.")
                expr
            }
            TokenType.CROCHET_OUV -> {
                advance()
                val elements = mutableListOf<NexusExpr>()
                if (!check(TokenType.CROCHET_FER)) {
                    do {
                        elements.add(parseExpression())
                    } while (match(TokenType.VIRGULE))
                }
                consume(TokenType.CROCHET_FER, "']' attendu.")
                NexusExpr.ListLiteral(elements)
            }
            TokenType.ACCOLADE_OUV -> {
                advance()
                val entries = mutableListOf<Pair<NexusExpr, NexusExpr>>()
                if (!check(TokenType.ACCOLADE_FER)) {
                    do {
                        val keyExpr = parseExpression()
                        consume(TokenType.DEUX_POINTS, "':' attendu dans la clé de l'objet.")
                        val valExpr = parseExpression()
                        entries.add(keyExpr to valExpr)
                    } while (match(TokenType.VIRGULE))
                }
                consume(TokenType.ACCOLADE_FER, "'}' attendu.")
                NexusExpr.MapLiteral(entries)
            }
            else -> {
                advance()
                NexusExpr.Literal(null)
            }
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val tok = peek()
        throw RuntimeException("Erreur de syntaxe ligne ${tok.line}:${tok.column} : $message (trouvé: ${tok.type})")
    }
}
