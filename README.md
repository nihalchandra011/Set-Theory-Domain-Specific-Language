## CS474_OOLE
# Set Theory
**Nihal Chandra**<br>
**UIN: 674916217**<br><br>
This project implements a Domain-Specific Language (DSL) using Scala for writing and evaluating set operations of Set Theory. Using this DSL, users can describe and evaluate binary operations on sets using variables and scopes where elements of the sets can be objects of any type.

**Execution Environment**<br>
This language is programmed in Scala (Version 3.1.0) and executed in the IntelliJ IDEA integrated Development Environment (Version 2021.3.2 - Ultimate Edition). Some outputs might vary if there is a change in the versions. Make sure you have Scala, IntelliJ, SBT and ScalaTest installed before implementing the language.

**Package Installation**<br>
To use this language, clone this GitHub Repository to you local machine and execute in IntelliJ by implementing the following steps:
- There are several ways to clone to your local machine. You can use HTTPS or SSH, amongst other options. Let’s use HTTPS as it can be the simplest option. 
- Copy the github link of this repository.
- When you first open the IntelliJ IDEA page, you see a screen with the  option 'Get from VCS'. Click on that to see a box where you can enter the Github URL. 
- Choose the directory where you want to load the project and click  on the 'Clone' button.
- Voila! The project is loaded into your IntelliJ. You can now run the program and execute the test cases.

NOTE -
<br>Homework 1 has been implemented in the Scala file located at ```src/test/scala/SetTheory.scala```.
<br>Homework 2 has been implemented in the Scala file located at ```src/test/scala/SetTheory2.scala```.
<br>Homework 3 has been implemented in the Scala file located at ```src/test/scala/SetTheory3.scala```.
<br>Homework 4 has been implemented in the Scala file located at ```src/test/scala/SetTheory4.scala```.
<br>Homework 5 has been implemented in the Scala file located at ```src/test/scala/SetTheory5.scala```.

**Set Operations**<br>
In this language, the following operations have been implemented:<br>
- **Value(input: Any)**<br>
  - Returns the value that is passed into it.
  - The value passed is of Any type.

- **Check(setName: String, element: SetExpression)**<br>
  - Checks if an element is contained in a set or not. 
  - Takes in two parameters - the Set Name and the element to be stored in it (could be an integer, a Double type or a string). 
  - It returns 'true' if the element is found or else 'false'.

- **Assign(varName: String, value: SetExpression)**<br>
  - Assigns the value obtained from the SetExpression to variable 'varName'. 
  - It will create a new one if the given variable name contained in op1 does not exist. 
  - Returns the value after assigning it.

- **Insert(varName: String, element: SetExpression)**<br> 
  - Inserts the element obtained from the SetExpression into the variable 'varName'.  
  - Does not allow you (throws an error) to add elements to variables that do not exist. 
  - Returns the value(s) stored in 'varName' after insertion of the element.

- **Delete(varName: String, element: SetExpression)**<br> 
  - Deletes an element obtained from the SetExpression, from the set 'varName'.
  - Ignores the deletion if the set does not exist itself.
  - Returns the set after deletion.
   
- **Union(set1: String, set2: String)**<br> 
  - Performs the union of the two sets 'set1' and 'set2'.
  - In other words, it returns all the elements present in both set1 and set2.
  - Returns the set obtained after performing the union operation.

- **Intersection(set1: String, set2: String)**<br> 
  - Performs the union of the two sets 'set1' and 'set2'.
  - In other words, it returns only the common elements present in both set1 and set2.
  - Returns the set obtained after performing the union operation.

- **SetDifference(set1: String, set2: String)**<br> 
  - Performs the set difference of the two sets 'set1' and 'set2'.
  - In other words, it returns the elements present set1 but not in set2.
  - Returns the set obtained after performing the difference operation.

- **SymmetricDifference(set1: String, set2: String)**<br> 
  - Performs the symmetric difference of the two sets 'set1' and 'set2'.
  - In other words, it returns the elements  that are a member of exactly one of set1 and set2 (elements which are in one of the sets, but not in both).
  - Returns the set obtained after finding their Symmetric Difference .

- **CartesianProduct(set1: String, set2: String)**<br> 
  - Performs the union of the two sets 'set1' and 'set2'.
  - In other words, it returns the elements that are all possible ordered pairs (a, b), where a is a member of set1 and b is a member of set2.
  - Returns the set obtained after finding the Cartesian Product of both the sets.

- **assignMacro(macroName: String, macroOp: SetExpression)**<br> 
  - Assigns the SetExpression contained in 'marcoOp' to the 'macroName' so that it can be easily referenced later through the marcoName.

- **resolveMacro(macroName: String)**<br> 
  - Resolves the 'macroName' and implements its corresponding SetExpression.
  - Returns the result obtained after performing the corresponding operation stored as 'macroName'.

**Class Constructs**<br>
The following datatypes have been used to implement classes, objects, methods and their interaction including inheritance.

- **ClassDef(className: String, classContents: Expression)**<br>
  - Creates a blueprint of the class and adds it to the mapping.
  - If there are any field values, initializes it to 0.
  - Constructors and Methods are not evaluted just yet. They are executed when the they are called using the corresponding class object.

- **Field(fieldName: String)**<br>
  - The field is defined here.
  - Its is given a default value 0.

- **Constructor(className: String, expressions: Expression)**<br>
  - This is invoked when a new object is created.
  - Initializes the values of the instance variables.
  - Can execute multiple number of initializations.
  - Passing parameters to constructors is not implemented.

- **Method(methodName: String, methodsExp: Expression)**<br>
  - Implements the operations present inside the method.
  - Returns the result of the last operation.
  - Can take in and execute multiple operations inside it.
  - Passing parameters to methods is not implemented.

- **case NewObject(className: String, objName: String)**<br>
  - Creates a new object of the given className.
  - Initializes the values defined in the class constructor.
  - The given language currently works with only two objects at a time.
  - Returns the objName after creation.
 
- **InvokeMethod(objName: String, methodName: String)**<br>
  - Invokes the method 'methodName' present in the class of the object 'objName' passed as parameter.
  - Returns the result of the method after execution.

- **Extends(subClassName: String, superClassName: String)**<br>
  - Implements the conventional 'extends' keyword.
  - This expression gets in all the methods and variables of the superclass into the subclass.
  - Prevents Multiple Inheritance.

- **AbstractClassDef(abstractClassName: String, abstractClassContents: Operations)**<br>
  - This construct defines an abstract class.
  - It should contain atleast 1 abstract method. (Will give an error message otherwise)
  - In this construct, it is only created and the corresponding mappings are added.
  - Returns the Class -> Method mappings after definition.

- **AbstractMethod(methodName: String)**<br>
  - Since it is abstract, its definition is not given.
  - In this construct, only its mappings are added and returned.

- **InterfaceDecl(interfaceName: String, abstractMethods: Operations)**<br>
  - An interface is created with the name 'interfaceName'.
  - It contains only abstract method.
  - The default method has not been implemented.
  - The abstract methods are defined when this interface is implemented.
  - Returns mapping of interface to abstract methods.

- **Implements(subClassName: String, interfaceName: String)**<br>
  - The class 'subClassName' implemnts the 'interfaceName' interface.
  - The parameters represent the following: subClassName - class, abstract class or interface | interfaceName: Interface
  - Can implement multiple interfaces.

**Branching and Exception Handling Constructs**<br>
The following constructs have been used to implement branching using if-else and exception handling using try-catch block.

- **IF(condition: Operations, thenClause: Set[Operations], elseClause: Set[Operations])**<br>
  - This construct defines the use of if-else branching similar to OOP Languages.
  - Follows lazy evaluation, i.e,  if the condition is evaluated to true then only thenClause is evaluated and elseClause is not.
  - The thenClause and elseClause are given as a Set of operations.
  - The Operations can be any of the set operations or another If-else construct.
  - Nested If constructs are also implemented.

- **Scope(scopeName: String, expressions: Operations)**<br>
  - Defines the code region within which the bindings are active.
  - Although it has been implemented before, a definitive construct has been created here to be used whenever required.
  - The 'scopeName' is used to access the given scope.

- **ExceptionClassDef(exceptionClassName: String, reason:Operations)**<br>
  - Creates a class 'exceptionClassName' and pushes it to a stack to be accessed later.
  - A mapping of the 'reason' is created that associates the exceptionClassName with the reason.
  - Returns the mapping created between the exceptionClassName and reason.

- **ThrowException(exceptionClassName: Operations, exceptionDefinition: Operations)**<br>
  - Throws an exception whenever encountered.
  - Adds the value of the exception reason to the mapping mentioned previously.

- **CatchException(exceptionClassName: String, tryExpressions: Set[Operations], catchExpressions: Set[Operations])**<br>
  - This construct handles the major part of try-catch block. 
  - The 'exceptionClassName' is used to access and invoke the Exception Class whenever required.
  - The 'tryExpressions' and 'catchExpressions' contain a set of expressions which could be operations or if-else constructs.
  - The try block executes even if there is no exception thrown. But corresponding catch is obviously not evaluated in this case.

- **Catch(catchTreatment: Operations)**<br>
  - Evaluates the expressions present inside the Catch block of the Try-Catch.
  - This block only executes if an error is thrown.
  
**Not Implemented**<br>
The following concept has not been implemented in this language yet:
- Nested try-catch block execution.

**Testing**<br>
Using IntelliJ:
The tests are present under 
```src/test/scala/SetTesting.scala``` 
```src/test/scala/SetTesting2.scala```
```src/test/scala/SetTesting3.scala```
```src/test/scala/SetTesting4.scala```
```src/test/scala/SetTesting5.scala```
<br>Right-click and run the programs to check the test cases.

Using Terminal/Command Prompt:<br>
To run the SBT Tests from the Command Line, do the following:
  - Open the Command Prompt.
  - Navigate to the directory holding the current project.
  - Type ```sbt new scala/scalatest-example.g8``` and hit Enter.
  - Now run ```sbt test```.
  - You will be prompted to enter a template name. Give it an interesting name and hit Enter. 
  - Optionally, you can also do an ```sbt clean compile test``` from the command prompt to check the execution.
  - This will evaluate all the test cases provided in the Project.
  - If all of them execute, you will get a green success message.
