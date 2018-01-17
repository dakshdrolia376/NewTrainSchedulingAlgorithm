public class Pair<T, U> {
    public T t;
    public U u;

    Pair() {
    }

    @SuppressWarnings("unused")
    public Pair(T t, U u) {
        this.t= t;
        this.u= u;
    }

    public boolean updateFirst(T t){
        this.t = t;
        return true;
    }

    public boolean updateSecond(U u){
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