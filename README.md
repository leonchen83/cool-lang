# cool-lang

An interpreter of Classroom Object Oriented Language(Stanford cs143) written by Java10

## Requirements

```
    $java -version
    $java version "10"

    $mvn -version
    $Apache Maven 3.3.1

```

## Usage

```
    $cd cool-lang/
    $mvn clean package
    $cd target
    $java -jar cool-lang-1.0-SNAPSHOT-jar-with-dependencies.jar ../src/test/resources/hello.cl
```
  
`output:`

```
    Hello, world! chen bao yi
```

## Syntax examples

* sort_list.cl  

```
Class List inherits IO {
        (* Since abort() returns Object, we need something of
	   type Bool at the end of the block to satisfy the typechecker.
           This code is unreachable, since abort() halts the program. *)
	isNil() : Bool { { abort(); true; } };

	cons(hd : Int) : Cons {
	  (let new_cell : Cons <- new Cons in
		new_cell.init(hd,self)
	  )
	};

	(*
	   Since abort "returns" type Object, we have to add
	   an expression of type Int here to satisfy the typechecker.
	   This code is, of course, unreachable.
        *)
	car() : Int { { abort(); new Int; } };

	cdr() : List { { abort(); new List; } };

	rev() : List { cdr() };

	sort() : List { cdr() };

	insert(i : Int) : List { cdr() };

	rcons(i : Int) : List { cdr() };

	print_list() : Object { abort() };
};

Class Cons inherits List {
	xcar : Int;  -- We keep the car in cdr in attributes.
	xcdr : List; -- Because methods and features must have different names,
		     -- we use xcar and xcdr for the attributes and reserve
		     -- cons and car for the features.

	isNil() : Bool { false };

	init(hd : Int, tl : List) : Cons {
	  {
	    xcar <- hd;
	    xcdr <- tl;
	    self;
	  }
	};

	car() : Int { xcar };

	cdr() : List { xcdr };

	rev() : List { (xcdr.rev()).rcons(xcar) };

	sort() : List { (xcdr.sort()).insert(xcar) };

	insert(i : Int) : List {
		if i < xcar then
			(new Cons).init(i,self)
		else
			(new Cons).init(xcar,xcdr.insert(i))
		fi
	};


	rcons(i : Int) : List { (new Cons).init(xcar, xcdr.rcons(i)) };

	print_list() : Object {
		{
		     out_int(xcar);
		     out_string("\n");
		     xcdr.print_list();
		}
	};
};

Class Nil inherits List {
	isNil() : Bool { true };

        rev() : List { self };

	sort() : List { self };

	insert(i : Int) : List { rcons(i) };

	rcons(i : Int) : List { (new Cons).init(i,self) };

	print_list() : Object { true };

};


Class Main inherits IO {

	l : List;

	(* iota maps its integer argument n into the list 0..n-1 *)
	iota(i : Int) : List {
	    {
		l <- new Nil;
		(let j : Int <- 0 in
		   while j < i
		   loop
		     {
		       l <- (new Cons).init(j,l);
		       j <- j + 1;
		     }
		   pool
		);
		l;
	    }
	};

	main() : Object {
	   {
	     out_string("How many numbers to sort?");
	     iota(in_int()).rev().sort().print_list();
	   }
	};
};
```  

* factorial.cl  
  
```
class Main inherits IO {
    main(): Object {{
        out_string("Enter an integer greater-than or equal-to 0: ");
        let input: Int <- in_int() in
            if input < 0 then
                out_string("ERROR: Number must be greater-than or equal-to 0\n")
            else {
                out_string("The factorial of ").out_int(input);
                out_string(" is ").out_int(new Main1.factorial(input));
                out_string("\n");
            }
            fi;
    }};
    factorial(num: Int): Int {
        if num = 0 then 1 else num * factorial(num - 1) fi
    };
};
class Main1 inherits IO {
    factorial(num: Int): Int {
        if num = 0 then 1 else num * factorial(num - 1) fi
    };
};
```  

* palindrome.cl
  
```
class Main inherits IO {
    pal(s : String) : Bool {
	if s.length() = 0
	then true
	else if s.length() = 1
	then true
	else if s.substr(0, 1) = s.substr(s.length() - 1, 1)
	then pal(s.substr(1, s.length() -2))
	else false
	fi fi fi
    };

    i : Int;

    main() : SELF_TYPE {
	{
            i <- ~1;
	    out_string("enter a string\n");
	    if pal(in_string())
	    then out_string("that was a palindrome\n")
	    else out_string("that was not a palindrome\n")
	    fi;
	}
    };
};
```

## References

  * [The Cool Reference Manual](https://spark-university.s3.amazonaws.com/stanford-compilers/resources%2Fcool_manual.pdf)
  * [Video Lectures](https://class.coursera.org/compilers-004/lecture)
  * [The shunting yard algorithm](http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm)
  * [Java Tokenizer,Parser](http://hg.openjdk.java.net/jdk8/jdk8/langtools/)



