#main strings args#
println("a");
newThread({
    #run#
    sleep(1000);
    println("abc");
});
abc();
println("a");
println("a");
#abc#
println("b");