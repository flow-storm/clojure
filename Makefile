install:
	mvn -Dmaven.test.skip=true clean 
	mvn -Dmaven.test.skip=true package
	mvn -Dmaven.test.skip=true install
