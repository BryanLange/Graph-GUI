import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedList;

public class GG2482 {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
		}
		new GraphGUI();
	} // end main
//-----------------------------------------------------------------------------	
	
	// Creates a GUI window with a panel and buttons to build and simulate a graph
	@SuppressWarnings("serial")
	public static class GraphGUI extends JFrame {
		
		private JRadioButton addVertex, addEdge, removeVertex, removeEdge, moveVertex;
		private JButton addAllEdges, connectedComponents, showCutVertices, defaultColors, clearGraph, help;
		private GraphPanel graph;
		
		// Constructor initializes the GUI
		public GraphGUI() {
			JFrame frame = new JFrame("GraphGUI - 2482");
			frame.setSize(1000, 800);
			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			
			JPanel buttonPanel = new JPanel();
			BoxLayout boxLayout = new BoxLayout(buttonPanel, BoxLayout.Y_AXIS);
			buttonPanel.setLayout(boxLayout);
			ButtonGroup group = new ButtonGroup();
			addVertex = new JRadioButton("Add Vertex");
			group.add(addVertex);
			buttonPanel.add(addVertex);
			addEdge = new JRadioButton("Add Edge");
			group.add(addEdge);
			buttonPanel.add(addEdge);
			removeVertex = new JRadioButton("Remove Vertex");
			group.add(removeVertex);
			buttonPanel.add(removeVertex);
			removeEdge = new JRadioButton("Remove Edge");
			group.add(removeEdge);
			buttonPanel.add(removeEdge);
			moveVertex = new JRadioButton("Move Vertex");
			group.add(moveVertex);
			buttonPanel.add(moveVertex);
			
			ButtonListener listener = new ButtonListener();
			addAllEdges = new JButton("Add All Edges");
			addAllEdges.addActionListener(listener);
			buttonPanel.add(addAllEdges);		
			connectedComponents = new JButton("Connected Components");
			connectedComponents.addActionListener(listener);
			buttonPanel.add(connectedComponents);			
			showCutVertices = new JButton("Show Cut Vertices");
			showCutVertices.addActionListener(listener);
			buttonPanel.add(showCutVertices);
			defaultColors = new JButton("Default Colors");
			defaultColors.addActionListener(listener);
			buttonPanel.add(defaultColors);
			clearGraph = new JButton("Clear Graph");
			clearGraph.addActionListener(listener);
			buttonPanel.add(clearGraph);			
			help = new JButton("Help");
			help.addActionListener(listener);
			buttonPanel.add(help);
			frame.add(buttonPanel, BorderLayout.WEST);
			
			graph = new GraphPanel();
			frame.add(graph, BorderLayout.EAST);
			
			frame.setVisible(true);
		} // end GraphGUI constructor
		
   //--------------------------------------------------------------------------		
		// Enables reaction to buttons, when clicked
		private class ButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				if(event.getSource() == addAllEdges) {
					graph.addAllEdges();
				}
				if(event.getSource() == connectedComponents) {
					graph.connectedComponents();
				}
				if(event.getSource() == showCutVertices) {
					graph.cutVertices();
				}
				if(event.getSource() == defaultColors) {
					graph.resetColors();
				}
				if(event.getSource() == clearGraph) {
					graph.clearGraph();
				}
				if(event.getSource() == help) {
					new Help();
				}
			}
		} // end ButtonListener class
		
   //--------------------------------------------------------------------------	
		// Enables reaction to mouse events on the graph picture panel
		private class MouseListener extends MouseAdapter {
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				
				if(addVertex.isSelected()) {
					graph.addVertex(x, y);
				}
				if(addEdge.isSelected()) {
					graph.addEdge(x, y);
				}
				if(removeVertex.isSelected()) {
					graph.removeVertex(x, y);
				}
				if(removeEdge.isSelected()) {
					graph.removeEdge(x, y);
				}
			}
			public void mousePressed(MouseEvent e) {
				if(moveVertex.isSelected()) {
					graph.setToBeMoved(e.getX(), e.getY());
				}
			}
			public void mouseDragged(MouseEvent e) {
				if(moveVertex.isSelected()) {
					graph.moveVertex(e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(moveVertex.isSelected()) {
					graph.revertVertex();
				}
			}
		} // end MouseListener class	

   //--------------------------------------------------------------------------
		// Simulates a graph picture
		public class GraphPanel extends JPanel {
			private LinkedList<Vertex> vertices;
			private LinkedList<Edge> edges;
			private Vertex mark;
			private Color prev;
			
			// Initializes to graph picture panel
			public GraphPanel() {
				setPreferredSize(new Dimension(825, 500));
				setBackground(Color.white);
				MouseListener listener = new MouseListener();
				addMouseListener(listener);
				addMouseMotionListener(listener);
				vertices = new LinkedList<>();
				edges = new LinkedList<>();
				mark = null;
			} // end GraphPicturePanel constructor
			
			
			// Paints the current vertices and edges of the graph
			// 	with their respective colors.
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				for(Edge e: edges) {
					Graphics2D g2 = (Graphics2D) g;
	                g2.setStroke(new BasicStroke(2));
					g2.setColor(e.getColor());
					g2.draw(e.getLine());
				}
				for(Vertex v: vertices) {
					g.setColor(v.getColor());
					g.fillOval(v.getX()-7, v.getY()-7, 14, 14);
				}
			    repaint();
			} // end paintComponent()
			
			
			// Adds a vertex to the graph
			public void addVertex(int x, int y) {
				vertices.add(new Vertex(x, y));
			} // end addVertex()
			
			
			// Removes a vertex from the graph
			public void removeVertex(int x, int y) {
				Vertex expel = inRange(x, y);
				if(expel != null) {
					LinkedList<Edge> removeEdges = new LinkedList<>();
					for(Edge e: edges) {
						if(expel == e.getV1()) {
							removeEdges.add(e);
							continue;
						}
						else if(expel == e.getV2()) {
							removeEdges.add(e);
						}
					}
					for(Edge e: removeEdges) {
						edges.remove(e);
					}
					vertices.remove(expel);
					resetColors();
				}
			} // end removeVertex()
			
			
			// Adds an edge to the graph
			public void addEdge(int x, int y) {
				Vertex click = inRange(x, y);
				if(mark == null) {
					if(click != null) {
						mark = click;
						mark.setColor(Color.green);
					}
				}
				else if(mark != null && click != null) {
					edges.add(new Edge(mark, click));
					mark.setColor(Color.blue);
					mark = null;
				}
			} // end addEdge()
			
			
			// Removes an edge from the graph
			public void removeEdge(int x, int y) {
				Edge remove = null;
				for(Edge e: edges) {
					if(e.inRange(x, y)) {
						remove = e;
						resetColors();
					}
				}
				edges.remove(remove);
			} // removeEdge()
			
			
			// Returns a Vertex that is in range of a specified x,y coordinate
			//  Returns null if there is no Vertex in range
			public Vertex inRange(int x, int y)	{
				Point click = new Point(x, y);
				Vertex inRange = null;				
				for(Vertex v: vertices) {
					if(click.distance(v.getPoint()) < 10.0) {	
						inRange = v;
					}	
				}
				return inRange;
			} // end inRange()
				
			
			// Marks a Vertex that is to be moved when Move Vertex button is selected
			public void setToBeMoved(int x, int y) {
				if(inRange(x, y) != null) {
					mark = inRange(x, y);
					prev = mark.getColor();
					mark.setColor(Color.green);
				}
			} // end setToBeMoved()
			
			
			// Updates the position of the Vertex that was marked to be moved
			public void moveVertex(int x, int y) {
				if(mark != null) {
					mark.setPoint(x, y);
					updateEdges(mark);
				}
			} // end moveVertex()
			
			
			// Updates the position of the Edges associated with the Vertex 
			//  that was marked to be moved
			public void updateEdges(Vertex v) {
				for(Edge e: edges) {			
					if(v == e.getV1()) {
						e.setLine(v, e.getV2());
					} else if(v == e.getV2()) {
						e.setLine(e.getV1(), v);
					}
				}
			} // end updateEdges
			
			
			// Reverts the Vertex that was marked to be moved to its original color
			public void revertVertex() {
				if(mark != null) mark.setColor(prev);
				mark = null;
			} // end revertVertex()
			
			
			// Removes all vertices and edges from the graph
			public void clearGraph() {
				vertices.clear();
				edges.clear();
			} // clearGraph()
			
			
			// Adds all possible edges to graph
			public void addAllEdges() {
				ArrayList<Vertex> temp = new ArrayList<>();
				for(Vertex v: vertices) {
					temp.add(v);
				}
				edges.clear();
				while(!temp.isEmpty()) {
					Vertex h = temp.remove(0);
					for(Vertex v: temp) {
						edges.add(new Edge(h, v));
					}
				}
			} // end addAllEdges()
			
			
			// Displays all the connected components of a graph in different colors
			public void connectedComponents() {
				Color[]	colors = {Color.blue, Color.red, Color.green, Color.orange,
							    Color.cyan, Color.magenta, Color.pink, Color.yellow};
				int pickColor = 0;
				LinkedList<Vertex> totalKnown = new LinkedList<>();
				LinkedList<Vertex> known = new LinkedList<>();
				
				for(Vertex v: vertices) {
					if(!totalKnown.contains(v)) {
						depthFirstSearch(v, known, edges);
						
						if(pickColor == 8) pickColor = 0;
						for(Vertex y: known) {
							y.setColor(colors[pickColor]);
							for(Edge e: outgoingEdges(y, edges)){
								e.setColor(colors[pickColor]);
							}
							totalKnown.add(y);
						}
						known.clear();
						pickColor++;
					}
				}
			} // end connected Components()
			
			
			// Graph algorithm to determine connected components of a 
			//  graph with specified lists of vertices and edges
			public void depthFirstSearch(Vertex u, LinkedList<Vertex> known, LinkedList<Edge> edges) {
				known.add(u);
				for(Edge e: outgoingEdges(u, edges)) {
					Vertex v = opposite(u, e);
					if(!known.contains(v)) {
						depthFirstSearch(v, known, edges);
					}
				}			
			} // end depthFirstSearch()
			
			
			// Returns a list of all outgoing edges from a specified Vertex
			public LinkedList<Edge> outgoingEdges(Vertex u, LinkedList<Edge> edges){
				LinkedList<Edge> outgoingEdges = new LinkedList<>();
				for(Edge e: edges) {
					if(e.contains(u)) {
						outgoingEdges.add(e);
					}
				}
				return outgoingEdges;
			} // end outgoingEdges()
			
			
			// Given a specified Edge and Vertex, 
			//  returns the opposite Vertex of the specified Vertex 
			public Vertex opposite(Vertex u, Edge e) {
				if(e.getV1() == u) return e.getV2();
				else return e.getV1();
			} // end opposite()
			
			
			// Displays the Cut Vertices of a graph by temporarily removing each Vertex in the graph
			//  and seeing if the removal partitions the graph into multiple components
			public void cutVertices() {
				resetColors();
				int initialComponentNum = countComponent(vertices, edges);
				for(Vertex v: vertices) {
					LinkedList<Vertex> tempV = new LinkedList<>();
					for(Vertex y: vertices) tempV.add(y);
					tempV.remove(v);
					LinkedList<Edge> tempE = new LinkedList<>();
					for(Edge e: edges) tempE.add(e);
					disconnectEdges(tempE, v);
					if(countComponent(tempV, tempE) > initialComponentNum) {
						v.setColor(Color.red);
					}
				}
			} // end cutVerticesBruteF()
			
			
			// Counts the number of connected components in a
			//  specified graph with lists of vertices and edges			
			public int countComponent(LinkedList<Vertex> vertices, LinkedList<Edge> edges) {
				LinkedList<Vertex> totalKnown = new LinkedList<>();
				LinkedList<Vertex> known = new LinkedList<>();
				int count = 0;
				
				for(Vertex v: vertices) {
					if(!totalKnown.contains(v)) {
						depthFirstSearch(v, known, edges);
						count++;
						for(Vertex y: known) {
							totalKnown.add(y);
						}
						known.clear();
					}
				}
				return count;
			} // end countComponent()
			
			
			// Removes the all the associated edges of a given Vertex 
			//  from a specified list of edges
			public void disconnectEdges(LinkedList<Edge> edges, Vertex v) {
				LinkedList<Edge> toBeRemoved = new LinkedList<>();
				for(Edge e: edges) {
					if(e.contains(v)) toBeRemoved.add(e);
				}
				for(Edge e: toBeRemoved) {
					edges.remove(e);
				}
			} // end disconnectEdges()
			
			
			// Resets the graph to default colors
			public void resetColors() {
				for(Vertex v: vertices) v.setColor(Color.blue);
				for(Edge e: edges) e.setColor(Color.black);
			} // end resetColors()
			
		} // end GraphPanel class	
	
	} // end GraphGUI class
		
//--------------------------------------------------------------------------------------------------	
	// Holds the Point of a vertex and its color
	public static class Vertex {
		private Point point;
		private Color color;
		
		public Vertex(int x, int y) {
			point = new Point(x, y);
			color = Color.blue;
		}
		public void setPoint(int x, int y) {
			point.setLocation(x, y);;
		}
		public Point getPoint() {
			return point;
		}
		public int getX() {
			return point.x;
		}
		public int getY() {
			return point.y;
		}
		public void setColor(Color t) {
			color = t;
		}
		public Color getColor() {
			return color;
		}
	} // end Vertex class

//-----------------------------------------------------------------------------------------------
	// Holds the two Vertices that make up an edge and its color
	// Line2D used for edge removal simplification
	public static class Edge {
		private Vertex v1;
		private Vertex v2;
		private Line2D line;
		private Color color;
		
		public Edge(Vertex first, Vertex second) {
			v1 = first;
			v2 = second;
			line = new Line2D.Double(v1.getPoint(), v2.getPoint());
			color = Color.black;
		}
		public Vertex getV1() {
			return v1;
		}
		public Vertex getV2() {
			return v2;
		}
		public void setLine(Vertex v1, Vertex v2) {
			line.setLine(v1.getPoint(), v2.getPoint());
		}
		public Line2D getLine() {
			return line;
		}
		public void setColor(Color c) {
			color = c;
		}
		public Color getColor() {
			return color;
		}
		public boolean inRange(int x, int y) {
			if(line.ptLineDist(new Point(x, y)) < 10) {
				return true;
			}
			else return false;
		}
		public boolean contains(Vertex v) {
			if(v1 == v || v2 == v) return true;
			else return false;
		}
	} // end Edge class
		
//-----------------------------------------------------------------------------
	// Display window for the help screen
	public static class Help {
		public Help() {
			JFrame helpWindow = new JFrame("Help");
			helpWindow.setSize(800, 600);
			helpWindow.setResizable(false);
			helpWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			helpWindow.setLocationRelativeTo(null);
			helpWindow.setVisible(true);
			
			helpWindow.add(new JLabel("<html>"
					+ "Add Vertex: When selected, clicking on the right half of the GUI"
					+ " will generate a vertex at the location of the mouse click.<br/><br/>"
					+ "Add Edge: When selected, an edge is made by clicking close to one of the two vertices that"
					+ " specify its ends. After the first end<br/> vertex of an edge is selected it"
					+ " is highlighted in green until the second end has been selected too at"
					+ " which time the edge is<br/> added to the graph and drawn on the gui.<br/><br/>"
					+ "Remove Vertex: When selected, click on a vertex to remove it from the graph.<br/><br/>"
					+ "Remove Edge: When selected, any click near an edge removes it, but leaves"
					+ " the vertices at its ends.<br/><br/>"
					+ "Move Vertex: When selected, click on a vertex to be moved then drag the mouse to a new"
					+ " location to move the vertex.<br/><br/>"
					+ "Add All Edges: Adds in all possible edges between all pairs of vertices in"
					+ " the graph.<br/><br/>"
					+ "Connected Components: Shows the different components of the graph in different"
					+ " colors, if possible.<br/><br/>"
					+ "Show Cut Vertices: Highlights all the cut vertices of the graph, if any exist."
					+ " If a highlighted vertice is removed, the graph will be<br/> cut into components.<br/><br/>"
					+ "Default Colors: Resets all vertices to blue and all edges to black.<br/><br/>"
					+ "Clear Graph: Removes all vertices and edges from the graph.<br/><br/>"
					+ "Help: Displays the help screen."
					+ "<html>"
					));
			helpWindow.pack();
		}
	} // end help class
	
} // end GG2482 class
