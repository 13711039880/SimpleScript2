#main args#
frame.new(frame);
frame.setSize(frame,300,200);

frame.button.new(button);
frame.button.setText(button,"a");
frame.button.addActionListener(button,{
    #actionPerformed e#
    println("Hello World!!!");
});
frame.add(frame,button);

frame.setVisible(frame,true);