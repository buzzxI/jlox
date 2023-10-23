package icu.buzz.lox.stmt;

public interface StmtVisitor<R> {
    R visitStmt (Stmt.Expression stmt);
    R visitStmt (Stmt.Print stmt);
    R visitStmt (Stmt.Var stmt);
}
