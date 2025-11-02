package clojure.testfixtures;

public class Component {

    private final MyView view;

    private Component(MyView view) {
        this.view = view;
    }

    public MyView getMyView() {
        return view;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private MyView view;

        public Builder setMyView(MyView.Builder builder) {
            this.view = builder == null ? null : builder.build();
            return this;
        }

        public Builder setMyView(MyView view) {
            this.view = view;
            return this;
        }

        public Component build() {
            return new Component(view);
        }
    }
}
