//
// Generated by JTB 1.3.2
//

package syntaxtree;

/**
 * Grammar production:
 * f0 -> "("
 * f1 -> "("
 * f2 -> Type()
 * f3 -> ")"
 * f4 -> Identifier()
 * f5 -> ")"
 */
public class RegisterLoad implements Node {
   public NodeToken f0;
   public NodeToken f1;
   public Type f2;
   public NodeToken f3;
   public Identifier f4;
   public NodeToken f5;

   public RegisterLoad(NodeToken n0, NodeToken n1, Type n2, NodeToken n3, Identifier n4, NodeToken n5) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
      f3 = n3;
      f4 = n4;
      f5 = n5;
   }

   public RegisterLoad(Type n0, Identifier n1) {
      f0 = new NodeToken("(");
      f1 = new NodeToken("(");
      f2 = n0;
      f3 = new NodeToken(")");
      f4 = n1;
      f5 = new NodeToken(")");
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
}

