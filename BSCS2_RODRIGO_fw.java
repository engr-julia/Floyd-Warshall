public class BSCS2_RODRIGO_fw {
    public static final int INF = Integer.MAX_VALUE / 2;

    public int[][] floydWarshall(int[][] graph) {
        int n = graph.length;
        int[][] dist = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = graph[i][j];
            }
        }
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != INF && dist[k][j] != INF && 
                        dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        
        return dist;
    }

    public String formatMatrix(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[i][j] == INF) {
                    sb.append(String.format("%-6s", "∞"));
                } else {
                    sb.append(String.format("%-6d", matrix[i][j]));
                }
            }
            sb.append("\n");
        }
        String result = sb.toString();
        System.out.println("Formatted Matrix:\n" + result);
        return result;
    }

    public int[][] addNode(int[][] graph) {
        int n = graph.length;
        int[][] newGraph = new int[n + 1][n + 1];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newGraph[i][j] = graph[i][j];
            }
        }
        
        for (int i = 0; i < n; i++) {
            newGraph[i][n] = INF;
            newGraph[n][i] = INF;
        }
        newGraph[n][n] = 0;
        
        return newGraph;
    }

    public int[][] removeLastNode(int[][] graph) {
        int n = graph.length;
        if (n <= 1) return graph;
        
        int[][] newGraph = new int[n - 1][n - 1];
        
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                newGraph[i][j] = graph[i][j];
            }
        }
        
        return newGraph;
    }

    public int[][] getDemoGraph() {
        return new int[][] {
            {0, 5, INF, 10},
            {INF, 0, 3, INF},
            {INF, INF, 0, 1},
            {INF, INF, INF, 0}
        };
    }
}