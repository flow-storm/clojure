package clojure.storm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class FormLocation implements IForm {
	public int formId;
	public String sourceFile;
	public int line;
	public String formNamespace;

	private static Keyword READ_COND_KEY = Keyword.intern(null, "read-cond");
	private static Keyword ALLOW_KEY = Keyword.intern(null, "allow");
	
	public FormLocation(int formId,String sourceFile, String ns, int line) {        
		this.sourceFile = sourceFile;
		this.line = line;
		this.formId = formId;
		this.formNamespace = ns;
	}	

	public Object getForm() {
		try {
			Namespace ns = Namespace.find(Symbol.intern(formNamespace));
			
			Var.pushThreadBindings(RT.map(RT.CURRENT_NS, ns));

			IPersistentMap opts = RT.map(READ_COND_KEY, ALLOW_KEY);
            
			InputStream strm = RT.baseLoader().getResourceAsStream(sourceFile);
			if(strm != null) {
				LineNumberingPushbackReader rdr = new LineNumberingPushbackReader(new InputStreamReader(strm));

				// discard lines until right before this.line
                for(int l=0; l<line-1; l++) {rdr.readLine();}

				// read the form
				Object form = LispReader.read(rdr, opts);
				
				rdr.close();

				return form;
			}
			            
		} catch (IOException ioe) {
			System.out.println("ERROR : can't read form. File " + sourceFile + " line : " + line);
			return null;
		} finally {
			Var.popThreadBindings();
		}
		return null;
		
	}
	
	public Integer getId() {
		return formId;
	}
	
	public String getNs() {
		return formNamespace;
	}
	
	public Keyword getDefKind() {
		return null;
	}
}
