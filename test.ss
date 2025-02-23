#main args#
frame.new($frame%);
frame.setSize($frame%,300,200);

frame.button.new($button%);
println($button%);
frame.button.setText($button%,"Click me");
frame.button.addActionListener($button%,{
    #actionPerformed e#
    println("Hello World!!!");
});
println($button%);
frame.add($frame%,$button%);

frame.setVisible($frame%,true);