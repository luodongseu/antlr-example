// Groovy doesn't have a good parser generator like ANTLR as far as I know.
// But you can use a Builder pattern to accomplish something similar.

class Foo {
    int x, y
    def product() {
        x * y
    }
    static class Builder {
        int x, y
        def setX(x) {
            this.@x = x
            this    // chainable methods are so so sexy
        }
        def setY(y) {
            this.@y = y
            this
        }
        def build() {
            def f = new Foo();
            f.x = x;
            f.y = y;
            f
        }
    }
}

Foo f = new Foo.Builder().setX(5).setY(3).build();
println(f.product())