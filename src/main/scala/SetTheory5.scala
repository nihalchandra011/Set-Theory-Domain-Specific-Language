/*
Homework 5
Domain Specific Language (DSL)
Set Theory
*/

import SetTheory5.Operations.{Check, CheckSet, Intersection, NewObject}

import scala.collection.mutable
object SetTheory5:
  //The mappings below contain the associations between classes, objects, variables, methods, constructors and interfaces
  private val Binding: mutable.Map[String, Any] = mutable.Map("var"->10,"var2"->10, "str"->"string", "aSetName"->mutable.Set(1,2,3,4,5), "set1"->mutable.Set(10,20,30), "set2"->mutable.Set(30,40,50))
  private val methodMapping = mutable.Map[String, Map[String,Operations]]()
  private val fieldMapping = mutable.Map[String, Map[String,Any]]()
  private val constructorMapping = mutable.Map[String, Operations]()
  private val objectMapping = mutable.Map[String, String]()
  private val abstractMethodMapping = mutable.Map[String, Set[String]]()
  private val exceptionMapping = mutable.Map[String, String]()

  //Stacks controlling the scope of objects, classes and interfaces
  private val ObjectStack = mutable.Stack[Any]("global")
  private val ClassStack = mutable.Stack[String]()
  private val AbstractClassStack = mutable.Stack[String]()
  private val InterfaceStack = mutable.Stack[String]()
  private val ScopeStack = mutable.Stack[String]("global")

  //Flag values for evaluating conditions
  private val Flag = mutable.Map[String,Int]()
  private val Called = mutable.Map[String,Boolean]("called"->false)
  private val ab = mutable.Map[String,Boolean]("ab"->false)
  private val imp = mutable.Stack[Boolean]()
  private val errorThrown = mutable.Stack[Any](0)
  private val tcStack = mutable.Stack[Any]()
  
  //The type A is used in set operations to allow passing of both sets as well as Operations, eg. Variable("setName")
  type A = Either[Operations,mutable.Set[Any]]

  //The following expressions are executed when a corresponding case match is found
  enum Operations:
    // HW1 Constructs
    case Value(input: Any)
    case Variable(varName: String)
    case Assign(varName: String, value: Any)
    case Initialize(varName: String, value: Any)
    case Check(op1: Any, op2: Any)
    case CheckSet(setName: String, element: Any)
    case GetBinding(name: String)
    case Insert(varName: String, element: Any)
    case Delete(varName: String, element: Any)
    case Union[A](set1: A, set2: A)
    case Intersection[A](set1: A, set2: A)
    case SetDifference[A](set1: A, set2: A)
    case SymmetricDifference[A](set1: A, set2: A)
    case CartesianProduct[A](set1: A, set2: A)

    // HW2 Constructs
    case ClassDef(className: String, classContents: Operations*)
    case Field(fieldName: String)
    case Constructor(expressions: Operations*)
    case Method(methodName: String, methodsExp: Operations*)
    case NewObject(className: String, objName: String)
    case InvokeMethod(objName: String, methodName: String)
    case Extends(subClassName: String, superClassName: String)

    // HW3 Constructs
    case AbstractClassDef(abstractClassName: String, abstractClassContents: Operations*)
    case AbstractMethod(methodName: String)
    case InterfaceDecl(interfaceName: String, abstractMethods: Operations*)
    case Implements(subClassName: String, interfaceName: String*)

    // HW4 Constructs
    case IF(condition: Operations, thenClause: Set[Operations], elseClause: Set[Operations])
    case Scope(scopeName: String, expressions: Operations*)
    case ExceptionClassDef(exceptionClassName: String, reason:Operations)
    case ThrowException(exceptionClassName: Operations, exceptionDefinition: Operations)
    case CatchException(exceptionClassName: String, tryExpressions: Set[Operations], catchExpressions: Set[Operations])
    case Catch(catchTreatment: Operations*)

    // This checks if the expression contains anything without a binding (gives error)
    // In that case, it returns the same expression after partial evaluation
    private def tryEval[A](expression: A): Any = {
      try {
        expression.asInstanceOf[Operations].eval
      }
      catch {
        case e: Throwable =>
          tcStack.push(e.getMessage)
          expression
      }
    }

    //This is basically the main function that contains the definitions of all constructs
    def eval: Any =
      this match {
        //Returns the Value that is passed into it
        case Value(i) => i

        // Gets the value of any parameter present in Binding
        case GetBinding(name) =>
          Binding(name)

        //Returns the corresponding value stored in the 'varName' if it exists or else assigns it a value of 0
        case Variable(varName) =>
          // If an exception has occurred, then the varName is added to the exceptionMapping
          if (ScopeStack.top == "exception")
            exceptionMapping += varName -> ClassStack.top
            return exceptionMapping

          // Checks if the variable is already present in the binding
          //If present, then return its value, else creates a binding with default value 0
          if (Binding.contains(varName))
            Binding(varName)
          else
            Binding += (varName -> 0)
            Variable(varName)

        //Assigns the 'value' to the variableName 'varName'
        case Assign(varName, value) =>
          if (Binding.contains(varName))
            Binding(varName) -> value
          else
            Binding += varName -> value

        // Checks if the two operators that exist are equal else return partially evaluated expression
        case Check(operator1, operator2) =>
          operator1 match {
            case str: String if operator2.isInstanceOf[String] => if (Binding.contains(str) && Binding.contains(operator2.asInstanceOf[String]))
              if (Binding(str) == Binding(operator2.asInstanceOf[String]))
                true
              else
                false

            else if (Binding.contains(str) && !Binding.contains(operator2.asInstanceOf[String]))
              Check(Binding(str), operator2)

            else if (!Binding.contains(str) && Binding.contains(operator2.asInstanceOf[String]))
              Check(operator1, Binding(operator2.asInstanceOf[String]))

            else
              Check(operator1, operator2)
            case _ =>
          }

        case CheckSet(setName, element) =>
          if(Binding.contains(setName) && element.isInstanceOf[String])
            if(Binding(setName).asInstanceOf[mutable.Set[Any]].contains(Binding(element.asInstanceOf[String])))
              true
            else
              false

          else if(!Binding.contains(setName) && Binding.contains(element.asInstanceOf[String]))
            CheckSet(setName, Binding(element.asInstanceOf[String]))

          else if(Binding.contains(setName) && !Binding.contains(element.asInstanceOf[String]))
            CheckSet(Binding(setName).asInstanceOf[String], element)

          else if(Binding.contains(setName))
            if(Binding(setName).asInstanceOf[mutable.Set[Any]].contains(element))
              true
            else
              false
          else
            CheckSet(setName, element)

        //Inserts an element into the set
        //Creates a new set if it doesn't exist
        case Insert(varName, element) =>
          if (Binding.contains(varName))
            val newAddition = Binding(varName).asInstanceOf[mutable.Set[Any]] + element.asInstanceOf[Operations].eval
            Binding += (varName -> newAddition)
            Binding(varName)
          else
            Insert(varName,element.asInstanceOf[Operations].eval)

        //Deletes` an element from the set
        case Delete(varName, element) =>
          if (Binding.contains(varName))
            Binding(varName).asInstanceOf[mutable.Set[Any]] -= element.asInstanceOf[Operations].eval
          else
            Delete(varName, element.asInstanceOf[Operations].eval)

        case Union(set1, set2) =>
          val x = tryEval(set1)
          val y = tryEval(set2)

          x match {
            case value: mutable.Set[Any] if y.isInstanceOf[mutable.Set[Any]] => value.union(y.asInstanceOf[mutable.Set[Any]])
            case operations: Operations if y.isInstanceOf[Operations] => Union(operations, y.asInstanceOf[Operations])
            case operations: Operations => Union(operations, y.asInstanceOf[mutable.Set[Any]])
            case _ => y match {
              case operations: Operations => Union(x.asInstanceOf[mutable.Set[Any]], operations)
              case _ =>
            }
          }


        //Returns the Intersection of 'set1' and 'set2'
        case Intersection(set1, set2) =>
          val x = tryEval(set1)
          val y = tryEval(set2)

          x match {
            case value: mutable.Set[Any] if y.isInstanceOf[mutable.Set[Any]] => value.intersect(y.asInstanceOf[mutable.Set[Any]])
            case operations: Operations if y.isInstanceOf[Operations] => Intersection(operations, y.asInstanceOf[Operations])
            case operations: Operations => Intersection(operations, y.asInstanceOf[mutable.Set[Any]])
            case _ => y match {
              case operations: Operations => Intersection(x.asInstanceOf[mutable.Set[Any]], operations)
              case _ =>
            }
          }

        //Returns the Set Difference of 'set1' and 'set2'
        case SetDifference(set1, set2) =>
          val x = tryEval(set1)
          val y = tryEval(set2)

          x match {
            case value: mutable.Set[Any] if y.isInstanceOf[mutable.Set[Any]] => value.diff(y.asInstanceOf[mutable.Set[Any]])
            case operations: Operations if y.isInstanceOf[Operations] => SetDifference(operations, y.asInstanceOf[Operations])
            case operations: Operations => SetDifference(operations, y.asInstanceOf[mutable.Set[Any]])
            case _ => y match {
              case operations: Operations => SetDifference(x.asInstanceOf[mutable.Set[Any]], operations)
              case _ =>
            }
          }

        //Returns the Symmetric Difference of 'set1' and 'set2'
        case SymmetricDifference(set1, set2) =>
          val x = tryEval(set1)
          val y = tryEval(set2)

          x match {
            case value: mutable.Set[Any] if y.isInstanceOf[mutable.Set[Any]] => value.diff(y.asInstanceOf[mutable.Set[Any]]).
              union(y.asInstanceOf[mutable.Set[Any]].diff(value))
            case operations: Operations if y.isInstanceOf[Operations] => SymmetricDifference(operations, y.asInstanceOf[Operations])
            case operations: Operations => SymmetricDifference(operations, y.asInstanceOf[mutable.Set[Any]])
            case _ => y match {
              case operations: Operations => SymmetricDifference(x.asInstanceOf[mutable.Set[Any]], operations)
              case _ =>
            }
          }

        //Returns the Cartesian Product of 'set1' and 'set2'
        case CartesianProduct(set1, set2) =>
          val x = tryEval(set1)
          val y = tryEval(set2)

          x match {
            case value: mutable.Set[Any] if y.isInstanceOf[mutable.Set[Any]] =>
              val new_binding = mutable.Set[Any]()
              for (a <- value)
                for (b <- y.asInstanceOf[mutable.Set[Any]])
                  new_binding += ((a, b))
              new_binding
            case operations: Operations if y.isInstanceOf[Operations] => CartesianProduct(operations, y.asInstanceOf[Operations])
            case operations: Operations => CartesianProduct(operations, y.asInstanceOf[mutable.Set[Any]])
            case _ => y match {
              case operations: Operations => CartesianProduct(x.asInstanceOf[mutable.Set[Any]], operations)
              case _ =>
            }
          }


        //Creates a mapping associating the className with the field and its value (default: None)
        case Field(fieldName) =>
          // If an exception has occurred, then a fieldName is created and added to the fieldMapping
          if (ScopeStack.top == "exception") {
            fieldMapping += ClassStack.top -> Map()
            fieldMapping(ClassStack.top) += fieldName -> "None"
            return fieldMapping(ClassStack.top)(fieldName)
          }
          // Used when Abstract Classes are implemented
          if (ab("ab")) {
            fieldMapping += AbstractClassStack.top -> Map()
            fieldMapping(AbstractClassStack.top) += fieldName -> "None"
          }
          else {
            fieldMapping += ClassStack.top -> Map()
            fieldMapping(ClassStack.top) += fieldName -> "None"
          }
          fieldMapping(ClassStack.top)(fieldName)

        //Constructor
        case Constructor(expressions*) =>
          for (exp <- expressions)
            if (ab("ab"))
              constructorMapping += AbstractClassStack.top -> exp
            else
              constructorMapping += ClassStack.top -> exp

        //Execution of the Method Construct
        case Method(methodName, methodsExp*) =>
          for (exp <- methodsExp)
          // Used when Abstract Classes are implemented
            if (ab("ab")) {
              if (methodMapping.contains(AbstractClassStack.top))
                methodMapping(AbstractClassStack.top) += methodName -> exp
              else
                methodMapping += AbstractClassStack.top -> Map()
              methodMapping(AbstractClassStack.top) += methodName -> exp
            }
            // This code below is the default implementation otherwise
            else {
              if (methodMapping.contains(ClassStack.top))
                methodMapping(ClassStack.top) += methodName -> exp
              else
                methodMapping += ClassStack.top -> Map()
                methodMapping(ClassStack.top) += methodName -> exp
            }
          methodMapping

        //Creates a new object and initializes the values defined in the class constructor
        case NewObject(className, objName) =>
          //Returning the expression as it is if the class does not exist
          if(!ClassStack.contains(className))
            return NewObject(className, objName)
          //Preventing Abstract Classes to be instantiated
          if (abstractMethodMapping.contains(className)) return className + " cannot be instantiated."
          ObjectStack.push(objName)
          objectMapping += (objName -> className)
          Called("called") = true
          // Evoking the corresponding class constructor
          if (constructorMapping.contains(className))
            constructorMapping(className).eval
          fieldMapping(className)

        //Assigns the 'value' to the variableName 'varName'
        case Initialize(varName, value) =>
          // If an exception has occurred, then the varName is added to the fieldMapping
          if (ScopeStack.top == "exception") {
            if (fieldMapping(ClassStack.top).contains(varName))
              fieldMapping(ClassStack.top) += varName -> value
            else
              fieldMapping += ClassStack.top -> Map()
              fieldMapping(ClassStack.top) += varName -> value
            return fieldMapping(ClassStack.top)(varName)
          }
          //This part is executed only if an object is called
          else if (Called("called")) {
            Called("called") = false
            if (fieldMapping(objectMapping(ObjectStack.asInstanceOf[String])).contains(varName)) {
              fieldMapping(objectMapping(ObjectStack.asInstanceOf[String])) += varName -> value
            }
            else {
              println(varName + " does not exist.")
            }
          }
          // The code below is the default execution when Field is called
          else {
            if (fieldMapping(objectMapping(ObjectStack.asInstanceOf[String])).contains(varName)) {
              fieldMapping(objectMapping(ObjectStack.asInstanceOf[String])) += varName -> value
            }
            else {
              fieldMapping += objectMapping(ObjectStack.asInstanceOf[String]) -> Map()
              fieldMapping(objectMapping(ObjectStack.asInstanceOf[String])) += varName -> value
            }
          }
          fieldMapping

        //Invokes the method of the corresponding object
        case InvokeMethod(objName, methodName) =>
          if (ObjectStack.contains(objName))
            methodMapping(objectMapping(objName))(methodName).eval
          else
            InvokeMethod(objName, methodName)

        //Implements the conventional 'extends' keyword
        //This expression gets in all the methods and variables of the superclass into the subclass
        case Extends(subClassName, superClassName) =>
          //Checks for Cyclic inheritance
          if (subClassName == superClassName) return "Error: Cyclic inheritance: " + subClassName + " extends itself."
          Flag(subClassName) += 1
          // Check to prevent multiple inheritance
          if (Flag(subClassName) < 2) {
            //If an interface extends another interface
            if (InterfaceStack.contains(subClassName) && InterfaceStack.contains(superClassName)) {
              abstractMethodMapping += subClassName -> abstractMethodMapping(subClassName).++(abstractMethodMapping(superClassName))
              abstractMethodMapping
            }
            //If an interface extends an Abstract class
            //Check to prevent the interface to inherit from a pure abstract class
            else if (InterfaceStack.contains(subClassName) && abstractMethodMapping.contains(superClassName))
              "Interface cannot inherit from a pure abstract class."

            //If an abstract class extends another abstract class
            else if (abstractMethodMapping.contains(subClassName) && abstractMethodMapping.contains(superClassName)) {
              methodMapping += subClassName -> methodMapping(subClassName).++(methodMapping(superClassName))
              methodMapping
            }
            //If a concrete class extends an abstract class
            else if (methodMapping.contains(subClassName) && abstractMethodMapping.contains(superClassName)) {
              methodMapping += subClassName -> methodMapping(subClassName).++(methodMapping(superClassName))
              // Checking to see if all abstract methods have been implemented or not
              for (methodName <- abstractMethodMapping(superClassName))
                if (methodMapping(subClassName).contains(methodName)) imp += true
                else
                  imp += false
                  return "Please implement all abstract methods."
              methodMapping
            }
            //If a concrete class extends another concrete class
            else if (methodMapping.contains(subClassName) && methodMapping.contains(superClassName)) {
              methodMapping += subClassName -> methodMapping(subClassName).++(methodMapping(superClassName))
              return methodMapping
            }
          }
          else "Multiple Inheritance is not allowed."

        //Defines the class definition and stores it in the blueprint in the mapping
        case ClassDef(className, classContents*) =>
          ClassStack.push(className)
          Flag += className -> 0
          for (exp <- classContents)
            exp.eval
          methodMapping

        //Definition of Abstract Classes
        case AbstractClassDef(abstractClassName, abstractClassContents*) =>
          ab("ab") = true
          Flag += abstractClassName -> 0
          val abMethodPresent = mutable.Map[String, Boolean]("abMethodPresent" -> false)
          AbstractClassStack.push(abstractClassName)
          for (exp <- abstractClassContents)
            exp.eval
            if (exp.eval == abstractMethodMapping)
              abMethodPresent("abMethodPresent") = true
          if (!abMethodPresent("abMethodPresent")) return "Please define at least one abstract method since this class is abstract."
          AbstractClassStack.pop()
          ab("ab") = false
          methodMapping

        case AbstractMethod(methodName) =>
          if (ab("ab")) {
            if (abstractMethodMapping.contains(AbstractClassStack.top))
              abstractMethodMapping(AbstractClassStack.top) += methodName
            else {
              abstractMethodMapping += AbstractClassStack.top -> Set()
              abstractMethodMapping(AbstractClassStack.top) += methodName
            }
          }
          else {
            if (abstractMethodMapping.contains(InterfaceStack.top))
              abstractMethodMapping(InterfaceStack.top) += methodName
            else {
              abstractMethodMapping += InterfaceStack.top -> Set()
              abstractMethodMapping(InterfaceStack.top) += methodName
            }
          }
          abstractMethodMapping

        case InterfaceDecl(interfaceName, abstractMethods*) =>
          InterfaceStack.push(interfaceName)
          Flag += interfaceName -> 0
          for (method <- abstractMethods)
            method.eval
          //          InterfaceStack.pop()
          abstractMethodMapping

        case Implements(subClassName, interfaceName*) =>
          for (name <- interfaceName) {
            if (InterfaceStack.contains(subClassName) && InterfaceStack.contains(name))
              return "An interface cannot implement another interface."
            else if (abstractMethodMapping.contains(subClassName) && !InterfaceStack.contains(subClassName) && InterfaceStack.contains(name)) {
              abstractMethodMapping += subClassName -> abstractMethodMapping(subClassName).++(abstractMethodMapping(name))
              return abstractMethodMapping
            }
            else if (methodMapping.contains(subClassName) && InterfaceStack.contains(name)) {
              for (methodName <- abstractMethodMapping(name))
                if (methodMapping(subClassName).contains(methodName))
                  imp += true
                  return "Implementation successful!"
                else
                  imp += false
                  return "Please implement all abstract methods."
              return methodMapping
            }
          }

        // IF construct performing lazy-evaluation using condition, thenClause, elseClause
        case IF(condition, thenClause, elseClause) =>
          ScopeStack.push("IF")
            if (condition.eval == true)
                for (clause <- thenClause)
                  return clause.eval
            else
              for (clause <- elseClause)
                return clause.eval

          ScopeStack.pop()

        // Defining the Exception Class and mapping its reason with it
        case ExceptionClassDef(exceptionClassName, reason) =>
          ClassStack.push(exceptionClassName)
          reason.eval
          fieldMapping

        // Throws an Exception of the 'exceptionClassName'
        case ThrowException(exceptionClassName, _) =>
          exceptionClassName.eval
          // Pushing 1 to errorThrown to indicate throw of exception
          errorThrown.push(1)
          errorThrown
        //          throw Exception(exceptionDefinition.eval.asInstanceOf[String])

        // Handles the entire try-catch execution
        case CatchException(exceptionClassName, tryExpressions, catchExpressions) =>
          if !ScopeStack.contains("exception") then
            ScopeStack.push("exception")
          if !ClassStack.contains(exceptionClassName) then
            ClassStack.push(exceptionClassName)
          // tcStack takes care of nested try-catch blocks
          tcStack.push(1)

          // Try block execution
          for (tExp <- tryExpressions)
            if (errorThrown.head == 0)
              tExp.eval

          // Catch block execution
          if (errorThrown.head == 1)
            tcStack.pop()
            for (cExp <- catchExpressions)
              cExp.eval
            if (tcStack.isEmpty)
              errorThrown.pop()
          ClassStack.pop()


        // Catch and treat the exceptions and evaluate all the remaining exceptions as well
        case Catch(catchTreatment*) =>
          for (exp <- catchTreatment)
            exp.eval
          ScopeStack.pop()
        //          ClassStack.pop()

        // Handles the scope of any code block
        case Scope(scopeName, expressions*) =>
          ScopeStack.push(scopeName)
          for (exp <- expressions)
            exp.eval
          ScopeStack.pop()
      }



