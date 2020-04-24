package sybyline.satiafenris.ene.obf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import jdk.nashorn.api.scripting.*;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.*;
import jdk.nashorn.internal.parser.*;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.*;
import sybyline.satiafenris.ene.NashornScript;
import sybyline.satiafenris.ene.Script;

@SuppressWarnings("unused")
public class JSAST {

	public static String load(String resource) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(JSAST.class.getResourceAsStream("/sybyline/satiafenris/ene/obf/" + resource)));
			StringBuffer buff = new StringBuffer();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) buff.append(line).append('\n');
			return buff.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		Options options = new Options("nashorn");
		options.set("anon.functions", true);
		options.set("parse.only", true);
		options.set("scripting", true);
		ErrorManager errors = new ErrorManager();
		Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
		Source source = Source.sourceFor("obftest", load("obftest.js"));
		Parser parser = new Parser(context.getEnv(), source, errors);
		FunctionNode function = parser.parse();
		function.accept(new ObfNodeVisitor());
		
//		NashornScript ns = (NashornScript)Script.nashorn();
		String original = function.toString(true);
		System.err.println(original);
		
		//ScriptUtils.parse(code, name, includeLoc);
	}

	public static final class ObfNodeVisitor extends NodeVisitor<LexicalContext> {
		
		public ObfNodeVisitor() {
			super(new LexicalContext());
		}
		
		@Override
		public boolean enterCallNode(CallNode callNode) {
			return super.enterCallNode(callNode);
		}
		
		private static final Field _property;
		static {
			Field f = null; try {
				f = AccessNode.class.getDeclaredField("property");
				f.setAccessible(true);
			} catch(Exception e) {
				e.printStackTrace();
			}
			_property = f;
		}
		private void setProperty(AccessNode accessNode, String newProperty) {
			try {
				_property.set(accessNode, newProperty);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public Node leaveCallNode(CallNode callNode) {
			System.out.println(callNode.toString(false));
			Expression expr = callNode.getFunction();
			if (expr instanceof AccessNode) {
				AccessNode ac = (AccessNode)expr;
				String prop = ac.getProperty();
				String parent = ac.getBase().toString(false);
				if ("obj".equals(parent)) {
					if ("bar_func".equals(prop)) {
						return new CallNode(
							callNode.getLineNumber(),
							callNode.getToken(),
							callNode.getFinish(),
							new AccessNode(ac.getToken(), ac.getFinish(), ac.getBase(), "foo_func"),
							callNode.getArgs(),
							callNode.isNew()
						);
					}
				}
			}
			return super.leaveCallNode(callNode);
		}
		
		@Override
		public boolean enterVarNode(VarNode varNode) {
//			System.out.println(varNode);
			return super.enterVarNode(varNode);
		}
		
		@Override
		public Node leaveVarNode(VarNode varNode) {
			return super.leaveVarNode(varNode);
		}
		
	}

}
