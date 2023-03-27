package clojure.storm;

import clojure.lang.Keyword;

interface IForm {
	Object getForm();
	Integer getId();
	String getNs();
	String getSourceFile();
	int getLine();
	
}
