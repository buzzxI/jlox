package icu.buzz.lox.stmt;

public interface StmtVisitor<R> {
    R visitStmt (Stmt.Expression stmt);
    R visitStmt (Stmt.Print stmt);
    R visitStmt (Stmt.Block stmt);
    R visitStmt (Stmt.If stmt);
    R visitStmt (Stmt.Var stmt);
    R visitStmt (Stmt.Class stmt);
    R visitStmt (Stmt.Fun stmt);
    R visitStmt (Stmt.While stmt);
    R visitStmt (Stmt.Return stmt);
}
