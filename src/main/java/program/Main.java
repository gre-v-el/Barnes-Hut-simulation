package program;

import renderer.Window;

public class Main {
    public static void main(String[] args){
        // set and run the only window
        Window window = Window.get();

        window.run();
    }
}
