import static java.util.Objects.requireNonNull;

public class Pair<T, U> {
    public T t;
    public U u;

    Pair() {
    }

    public boolean updateFirst(T t){
        requireNonNull(t, "The new first value of pair is null.");
        this.t = t;
        return true;
    }

    public boolean updateSecond(U u){
        requireNonNull(u, "The new second value of pair is null.");
        this.u = u;
        return true;
    }

    public T getFirst(){
        return t;
    }

    public U getSecond(){
        return u;
    }
}