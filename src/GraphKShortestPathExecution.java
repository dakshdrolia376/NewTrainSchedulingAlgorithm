import java.util.List;

public class GraphKShortestPathExecution {

    public static void main(String[] args) {
        execution();
    }

    private static void execution() {
        GraphKBestPath<Character> graph = new GraphKBestPath<>();
        graph.addEdge(new Edge<>('a', 'b', 15.0));
        graph.addEdge(new Edge<>('a', 'c', 3.0));
        graph.addEdge(new Edge<>('c', 'b', 1.0));
        graph.addEdge(new Edge<>('a', 'd', 3.0));
        graph.addEdge(new Edge<>('d', 'c', 3.0));
        List<Path<Character>> paths = new DefaultKShortestPathFinder<Character>().findShortestPaths('a', 'b', graph, 4);
        System.out.println(paths.size());
        for(Path<Character> path:paths) {
            System.out.println(path.toString() + " cout: " + path.pathCost());
        }
    }
}