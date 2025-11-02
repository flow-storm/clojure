package clojure.testfixtures;

public class MyView {

    private final String id;

    private MyView(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String id = "";

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public MyView build() {
            return new MyView(id);
        }
    }
}
