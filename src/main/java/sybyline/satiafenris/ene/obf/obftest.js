///manual definevars obj map bar_func -> foo_func

function foo() {
	return 7;
}

var obj = {};
obj.foo_func = foo;

var seven = obj.bar_func();

console.log(seven);
