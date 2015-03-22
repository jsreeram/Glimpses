package prefuse.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.geom.Rectangle2D;
import java.awt.event.KeyEvent;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.math.BigInteger;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.NumberFormat;
import java.util.regex.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.lang.Object;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.swing.event.*;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

//Prefuse Packages
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.NodeItem;
import prefuse.visual.EdgeItem;
import prefuse.util.ui.JForcePanel;
import prefuse.util.FontLib;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.SpringForce;
import prefuse.util.force.DragForce;
import prefuse.util.force.Force;
import prefuse.util.ui.JFastLabel;
import prefuse.util.UpdateListener;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.render.EdgeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.ItemAction;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.render.AbstractShapeRenderer;
import prefuse.Constants;
import java.net.URL;

/**
 * @author Based on GraphView.java by <a href="http://jheer.org">jeffrey heer</a>
 * @author Modified for the GLIMPSES toolkit by Jaswanth Sreeram and Ashwini Bhagwat
 */

//Ashwini
class ListDialog extends JDialog
implements ActionListener {
	private static ListDialog dialog;
	private static String value = "";
	private JList list;

	public static String showDialog(Component frameComp,
			String title,
			String[] possibleValues,
			String initialValue
	) {
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		dialog = new ListDialog(frame,
				title,
				possibleValues,
				initialValue
		);
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return value;
	}

	private void setValue(String newValue) {
		value = newValue;
		list.setSelectedValue(value, true);
	}

	private ListDialog(Frame frame,
			String title,
			Object[] data,
			String initialValue
	) {
		super(frame, title, true);

		//Create and initialize the buttons.
		//
		final JButton setButton = new JButton("Set");
		final JButton quitButton = new JButton("Quit");
		setButton.setActionCommand("Set");
		quitButton.setActionCommand("Quit");
		setButton.addActionListener(this);
		quitButton.addActionListener(this);

		getRootPane().setDefaultButton(setButton);

		//main part of the dialog
		list = new JList(data) {
			public int getScrollableUnitIncrement(Rectangle visibleRect,
					int orientation,
					int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL &&
						direction < 0 && (row = getFirstVisibleIndex()) != -1) {
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0))  {
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);

						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(
						visibleRect, orientation, direction);
			}
		};

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					setButton.doClick(); //emulate button click
				}
			}
		});
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);

		//Create a container so that we can add a title around
		//the scroll pane.  Can't add a title directly to the
		//scroll pane because its background would be white.
		//Lay out the label and scroll pane from top to bottom.
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		//Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(setButton);
		buttonPane.add(quitButton);

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);

		//Initialize values.
		setValue(initialValue);
		pack();
		setLocationRelativeTo(frame);
	}

	//Handle clicks on the Set and Quit buttons.
	public void actionPerformed(ActionEvent e) {
		if ("Set".equals(e.getActionCommand())) {
			ListDialog.value = (String)(list.getSelectedValue());
			ListDialog.dialog.setVisible(false);
		}
		if("Quit".equals(e.getActionCommand())){
			System.exit(0);
		}
	}

}



//Rules for node coloring
class NodeColorAction extends ColorAction {

	public NodeColorAction(String group) {
		super(group, VisualItem.FILLCOLOR);
	}

	public int getColor(VisualItem item) {
		if(item.getString("external").equals("1"))
		{
			return ColorLib.rgb(255,255,255);
		}


		if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
			return ColorLib.rgb(150,150,255);
		else if ( m_vis.isInGroup(item, Visualization.FOCUS_ITEMS) )
		{
			if(item.getString("calls").equals("0"))
			{ 
				return ColorLib.rgb(255,0,0);
			}
			else
			{
				return ColorLib.rgb(0,180,0);
			}
		}
		else if ( item.getDOI() > -1 )
			return ColorLib.rgb(164,193,193);
		else
		{
			if(item.getString("calls").equals("0"))
			{ 
				return ColorLib.rgb(225,150,150);
			}
			else
			{
				return ColorLib.rgb(150,225,150);
			}
		}
	}

} // end of inner class TreeMapColorAction


class DynamicPopupMenu implements ActionListener, ItemListener
{
	prefuse.Display _display;
	VisualItem _item;
	String _curFuncName;
	String _workLoad;
	Visualization _mvis;

	public DynamicPopupMenu(MouseEvent e,Visualization m_vis, prefuse.Display display, VisualItem item ,String curFuncName, String workload)
	{
		m_vis.cancel("animatePaint");
		m_vis.run("fullPaint");
		m_vis.run("animatePaint");

		_item=item;
		_display=display;
		_curFuncName = curFuncName;	
		_workLoad= workload;
		createPopupMenu(e);
	}

	public void createPopupMenu(MouseEvent e) 
	{
		JMenuItem menuItem;
		JPopupMenu popup = new JPopupMenu();
		menuItem = new JMenuItem("Show Callees");
		menuItem.addActionListener(this);
		popup.add(menuItem);
		popup.show(e.getComponent(),e.getX(), e.getY());
	}


	public void actionPerformed(ActionEvent e) 
	{
		JMenuItem source = (JMenuItem)(e.getSource());
		//System.out.println("Selected menu item :"+source.getText());

		if(source.getText()== "Show Callees")
		{
			_item.getVisualization().repaint();
			System.out.println("Graphview.java: Showing Callees for function "+_curFuncName+"\n");	
			NodeItem n = (NodeItem)_item;
			Iterator iter = n.outNeighbors();
			while(iter.hasNext() ){
				NodeItem nitem = (NodeItem)iter.next();
				//	System.out.println(nitem.getString("name"));
				if(nitem.getString("external").equals("0"))
				{
					//System.out.println(nitem.getString("name"));
					nitem.setFillColor(ColorLib.rgb(255,255,0));
					nitem.setStrokeColor(nitem.getEndStrokeColor());
					nitem.getVisualization().repaint();
				}

			}
		}	
	}

	public void itemStateChanged(ItemEvent e) 
	{        
		System.out.println("State changed itemStateChanged.. \n");
	}

}


class MyPopupMenu implements ActionListener, ItemListener
{
	prefuse.Display _display;
	VisualItem _item;
	String _curFuncName;
	String _workLoad;
	String _funcPath;
	Visualization _mvis;

	public MyPopupMenu(MouseEvent e,Visualization m_vis, prefuse.Display display, VisualItem item ,String curFuncName, String workload,String funcPath)
	{
		_item=item;
		_display=display;
		_curFuncName = curFuncName;	
		_workLoad= workload;
		_mvis = m_vis;
		_funcPath = funcPath;

		_mvis.getGroup(Visualization.FOCUS_ITEMS).clear();
		_mvis.cancel("animatePaint");
		_mvis.run("fullPaint");
		_mvis.run("animatePaint");


		createPopupMenu(e);
	}

	public void createPopupMenu(MouseEvent e) 
	{
		JMenuItem menuItem;
		//Create the popup menu.
		JPopupMenu popup = new JPopupMenu();
		menuItem = new JMenuItem("Show CFG");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Show Callers");
		menuItem.addActionListener(this);
		popup.add(menuItem);			

		menuItem = new JMenuItem("Show Callees");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Show all reachable functions");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Select all reachable functions");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Show source");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		popup.show(e.getComponent(),e.getX(), e.getY());
	}

	public void showReachableNodes(NodeItem ni)
	{
		Iterator iter = ni.outNeighbors();
		while(iter.hasNext() ){
			NodeItem nitem = (NodeItem)iter.next();
			//System.out.println(nitem.getString("name"));
			nitem.setHighlighted(true);
			nitem.setFillColor(ColorLib.rgb(255,255,0));
			nitem.setStrokeColor(nitem.getEndStrokeColor());
			nitem.getVisualization().repaint();
			showReachableNodes(nitem);
		}

	}


	public void selectReachableNodes(NodeItem ni)
	{
		_mvis.getGroup(Visualization.FOCUS_ITEMS).addTuple(ni);
		Iterator iter = ni.outNeighbors();
		while(iter.hasNext() ){
			NodeItem nitem = (NodeItem)iter.next();

			VisualItem v = (VisualItem)nitem;
			_mvis.getGroup(Visualization.FOCUS_ITEMS).addTuple(v);
			nitem.getVisualization().repaint();
			selectReachableNodes(nitem);
		}

	}
	public void actionPerformed(ActionEvent e) 
	{
		JMenuItem source = (JMenuItem)(e.getSource());
		//System.out.println("Selected menu item :"+source.getText());

		//View CFG - create the ps file from the dot, and display in ghostview
		if(source.getText()== "Show CFG")
		{
			//	System.out.println("Graphview.java: Showing CFG for function "+ _curFuncName +"\n");
			File workDir = new File("../../../../WorkBench/Output/"+_workLoad+"_O/CFGs/");
			String psfile="cfg."+_curFuncName+".dot.ps";
			String cmd= "gv "+psfile;   

			try
			{
				Process ps = Runtime.getRuntime().exec(cmd, null, workDir);	
				int i = ps.waitFor();
			}
			catch(Exception ex)
			{
				//System.out.println("Graphview.java: Exception in ViewCFG Context Menu event handler");
			}
		}
		//View Callers - highlight nodes from which this function was called
		if (source.getText()== "Show Callers")
		{
			_item.getVisualization().repaint();
			//System.out.println("Graphview.java: Showing Callers for function "+_curFuncName+"\n");
			NodeItem n = (NodeItem)_item;
			Iterator iter = n.inNeighbors();
			while(iter.hasNext() ){
				NodeItem nitem = (NodeItem)iter.next();
				//	System.out.println(nitem.getString("name"));

				nitem.setFillColor(ColorLib.rgb(255,255,0));
				nitem.setStrokeColor(nitem.getEndStrokeColor());
				nitem.getVisualization().repaint();
			}		
		}

		//View Callees - highlight nodes that calll this function 
		if(source.getText()== "Show Callees")
		{
			_item.getVisualization().repaint();
			//System.out.println("Graphview.java: Showing Callees for function "+_curFuncName+"\n");	
			NodeItem n = (NodeItem)_item;

			Iterator iter = n.outNeighbors();
			while(iter.hasNext() ){
				NodeItem nitem = (NodeItem)iter.next();
				//System.out.println(nitem.getString("name"));
				nitem.setHighlighted(true);
				nitem.setFillColor(ColorLib.rgb(255,255,0));
				nitem.setStrokeColor(nitem.getEndStrokeColor());
				nitem.getVisualization().repaint();
			}
		}

		if(source.getText()== "Show all reachable functions")
		{
			_item.getVisualization().repaint();
			//System.out.println("Graphview.java: Selecting nodes below function "+_curFuncName+"\n");	

			NodeItem n = (NodeItem)_item;
			showReachableNodes(n);

		}

		if(source.getText()== "Select all reachable functions")
		{
			_item.getVisualization().repaint();
			//System.out.println("Graphview.java: Selecting nodes below function "+_curFuncName+"\n");	

			NodeItem n = (NodeItem)_item;
			selectReachableNodes(n);

		}

		if(source.getText()== "Show source")
		{

			_item.getVisualization().repaint();
			//System.out.println("Graphview.java: Showing source for function "+_curFuncName+"\n");	
			//System.out.println("Graphview.java: Opening file :"+_funcPath);


			String dir  = "../../../../WorkBench/Workloads/"+_workLoad+"/";
			String path = dir+ _funcPath;
			File workDir = new File(dir);
			//System.out.println(path);
			String cmd= "emacs "+_funcPath;   

			try
			{
				Process ps = Runtime.getRuntime().exec(cmd, null, workDir);	
				int i = ps.waitFor();
			}
			catch(Exception ex)
			{
				//System.out.println("Graphview.java: Exception in Show source Menu event handler");
			}
		}
	}

	public void itemStateChanged(ItemEvent e) 
	{        
		//System.out.println("State changed itemStateChanged.. \n");
	}

}

//Ashwini


class HelpTree extends JPanel implements TreeSelectionListener{
	private JEditorPane htmlPane;
	private JTree tree;

	private static boolean DEBUG = true;

	//Optionally play with line styles.  Possible values are
	//"Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal";

	//Optionally set the look and feel.
	private static boolean useSystemLookAndFeel = false;

	public HelpTree()
	{
		super(new GridLayout(1,0));

		//Create a tree that allows one selection at a time.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Glimpses Toolkit");
		createNodes(top);
		tree= new JTree(top);
		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		if (playWithLineStyle) {
			//System.out.println("line style = " + lineStyle);
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(tree);

		//Create the html viewing pane
		htmlPane= new JEditorPane();
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(300);
		splitPane.setPreferredSize(new Dimension(800, 500));

		//Add the split pane to this panel.
		add(splitPane);
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		tree.getLastSelectedPathComponent();

		if (node == null) return;

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			catInfo selectedTopic = (catInfo)nodeInfo;
			displayFile(selectedTopic.catURL); 
			//System.out.println("URL is "+ selectedTopic.catURL);
		} else 
		{
			//	           displayURL(helpURL);
		}
		if (DEBUG) {
			//System.out.println(nodeInfo.toString());
		}
	}

	private void displayFile(URL url) {
		try 
		{
			if(url!=null)
			{
				htmlPane.setPage(url);
			}
			else
			{
				htmlPane.setText("File not found");
			}

		} catch (IOException e) 
		{
			System.err.println("Unable to open file");
		}
	}

	private class catInfo {
		public String catName;
		public URL  catURL;

		public catInfo(String catname,String catFilename) {
			catName = catname;
			catURL = getClass().getResource(catFilename);
			if(catURL ==null)
			{
				System.err.println("Couldn find file: "+catFilename);
			}

		}

		public String toString() {
			return catName;
		}
	}
	private void createNodes(DefaultMutableTreeNode top)
	{
		DefaultMutableTreeNode category = null;
		category = new DefaultMutableTreeNode(new catInfo("Introduction","Help/introduction.html"));
		top.add(category);
		category = new DefaultMutableTreeNode(new catInfo("Prerequisites","Help/prerequisites.html"));
		top.add(category);
		category = new DefaultMutableTreeNode(new catInfo("Installation","Help/installation.html"));
		top.add(category);
		category = new DefaultMutableTreeNode(new catInfo("Using Glimpses","Help/profiling.html"));
		top.add(category);
		DefaultMutableTreeNode category1 = new DefaultMutableTreeNode(new catInfo("Profiling", "Help/profiling.html"));
		category.add(category1);

		DefaultMutableTreeNode visualization = new DefaultMutableTreeNode(new catInfo("Visualization", "Help/visualization.html"));
		category.add(visualization);
		DefaultMutableTreeNode sourceTree = new DefaultMutableTreeNode(new catInfo("SourceTree","Help/sourcetree.html"));
		visualization.add(sourceTree);


		//DefaultMutableTreeNode CallSequence = new DefaultMutableTreeNode(new catInfo("Call Sequence","Help/callsequence.html"));
		//visualization.add(CallSequence);

		DefaultMutableTreeNode callGraphs = new DefaultMutableTreeNode(new catInfo("Call Graphs","Help/staticgraph.html"));
		visualization.add(callGraphs);

		DefaultMutableTreeNode staticGraph = new DefaultMutableTreeNode(new catInfo("Static Call Graph","Help/staticgraph.html"));
		DefaultMutableTreeNode dynamicGraph = new DefaultMutableTreeNode(new catInfo("Dynamic Call Graph","Help/dynamicgraph.html"));
		callGraphs.add(staticGraph);
		callGraphs.add(dynamicGraph);

		DefaultMutableTreeNode functionProperties = new DefaultMutableTreeNode(new catInfo("Function Properties","Help/functionproperties.html"));
		visualization.add(functionProperties);

		DefaultMutableTreeNode MemoryMap = new DefaultMutableTreeNode(new catInfo("Memory Map","Help/memorymap.html"));
		DefaultMutableTreeNode FunctionCharacteristics = new DefaultMutableTreeNode(new catInfo("Metrics","Help/functioncharacteristics.html"));
		DefaultMutableTreeNode LibraryUtilRatio = new DefaultMutableTreeNode(new catInfo("Library Utilization Ratio","Help/libraryutilratio.html"));
		//DefaultMutableTreeNode AliasAnalysis = new DefaultMutableTreeNode(new catInfo("Alias Analysis","Help/aliasanalysis.html"));
		functionProperties.add(MemoryMap);
		functionProperties.add(FunctionCharacteristics);
		functionProperties.add(LibraryUtilRatio);
	}

}

class MyTree extends JPanel implements TreeSelectionListener {

	JSearchPanel sp;
	public JTree tree;
	private static String workloadpath ;
	private static boolean DEBUG = true;
	public static HashMap trip = new HashMap();  
	public static HashMap sourcefiles = new HashMap();

	//Optionally play with line styles.  Possible values are
	//"Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal";

	//Optionally set the look and feel.
	private static boolean useSystemLookAndFeel = false;

	public MyTree(String WorkLoadPath, JSearchPanel _sp) {

		super(new GridLayout(1,0));
		sp=_sp;
		workloadpath = WorkLoadPath;

		//Create a tree that allows one selection at a time.
		tree = new JTree(addNodes(null, new File("../../../../WorkBench/Workloads/"+WorkLoadPath)));
		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		if (playWithLineStyle) {
			//System.out.println("line style = " + lineStyle);
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(tree);

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);

		Dimension minimumSize = new Dimension(100, 50);
		//htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setPreferredSize(new Dimension(200, 1080));

		//Add the split pane to this panel.
		add(splitPane);

	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

		if (node == null) return;

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			FuncInfo selectedFunc = (FuncInfo)nodeInfo;

		} else 
		{

		}
		if (DEBUG) {
			//System.out.println(nodeInfo.toString());
		}
	}

	private class FuncInfo {
		public String funcName;
		public String funcPath;
		public TreePath nodePath;

		public FuncInfo(String funcname, String funcpath) {
			funcName = funcname;
			funcPath = funcpath;

		}

		public String toString() {
			return funcName;
		}
	}

	private void displayFile() {
		try 
		{
			//Open File in New Tab?

		} catch (RuntimeException e) 
		{
			System.err.println("Unable to open file");
		}
	}


	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) 
	{
		String curPath = dir.getPath();
		String truncCurPath = curPath;
		int ind = curPath.lastIndexOf('/',0);
		if(ind != -1)
		{
			truncCurPath = curPath.subSequence(0, ind).toString();
		}
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(new FuncInfo(truncCurPath,curPath));

		if (curTop != null) 
		{ // should only be null at root
			curTop.add(curDir);
		}
		Vector ol = new Vector();
		String[] tmp = dir.list();
		String cvs = "CVS";

		for (int i = 0; i < tmp.length; i++)
		{
			int index= tmp[i].indexOf(cvs);
			if(index== -1)
				ol.addElement(tmp[i]);
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		Vector files = new Vector();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) 
		{
			String thisObject = (String) ol.elementAt(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory())
				addNodes(curDir, f);
			else
				files.addElement(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++)
		{
			String fname = (files.elementAt(fnum)).toString();
			int in = fname.indexOf(".c");
			if(in != -1)
			{
				DefaultMutableTreeNode curFuncName = new DefaultMutableTreeNode(new FuncInfo(fname,curPath));
				curDir.add( curFuncName );

				try 
				{
					FileReader input1 = new FileReader("../../../../WorkBench/Output/"+workloadpath+"_O/nm.dump");
					BufferedReader bufRead = new BufferedReader(input1);
					String line;   
					int i=0;
					String[] tokens =null;                  	              	             
					line = bufRead.readLine();
					while(line!=null)
					{

						tokens = line.split(" ");
						String[] tokens1 =null;
						tokens1 = tokens[3].split("\t");
						//System.out.println("\n"+tokens1[0]+ " -- "  +tokens1[1]+ fname);
						int inde = tokens1[1].lastIndexOf("/");
						if(inde != -1)
						{
							int end = tokens1[1].length();
							String truncname  = tokens1[1].subSequence(inde+1, end).toString();

							int nn = truncname.indexOf(":");
							truncname = truncname.subSequence(0,nn).toString();

							int inde1 = fname.indexOf(truncname);
							if(inde1 != -1)
							{
								DefaultMutableTreeNode fnode = new DefaultMutableTreeNode(new FuncInfo(tokens1[0],curPath));
								curFuncName.add(fnode);
								TreePath t = new TreePath(fnode.getPath());
								trip.put(tokens1[0],t);
								sourcefiles.put(tokens1[0],fname);
							}
						}		

						line=bufRead.readLine();
					}
				}
				catch (IOException e){       
					//System.out.println("Graphview.java :"+"IO Exception in Function Reading nm.dump()");  	             
				}
				catch(RuntimeException e){
					//System.out.println("Graphview.java :"+"Runtime Exception in Function Reading nm.dump()");  	  
				}
			}
		}
		return curDir;
	}
}
class MyComboBox extends JPanel implements ActionListener {
	public String itemSelected;
	JComboBox comboBox;
	public MyComboBox(String[] List) 
	{
		super(new BorderLayout());
		comboBox = new JComboBox(List);
		comboBox.setSelectedIndex(0);	
		comboBox.addActionListener(this);

		//Lay out the combo box
		add(comboBox, BorderLayout.PAGE_START);
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	}

	//Listens for changes in the combo box
	public void actionPerformed(ActionEvent e) {
		JComboBox c = (JComboBox)e.getSource();
		itemSelected  =(String)c.getSelectedItem();
		updateTable(itemSelected);
	}

	public String GetIndex(){
		return itemSelected;
	}

	protected void updateTable(String name) {
		//Load the correct File into the text area based on the selection
	}
	public void setEnabled(boolean flag)
	{
		comboBox.setEnabled(flag);
	}

}


//Ashwini
class MyTableModel extends AbstractTableModel {

	private String[] columnNames = {"Parameter","Value"};
	private Object[][] data = {
			{"Code Size", " "},
			{"Used Code Size"," "},
			{"Code Util Ratio"," "},
			{"Stack Size", " "},
			{"Branch Density"," "},
			{"# of Vectorizable Loops", " "},
			{"Dynamic Memory Allocation"," "},
			{"No of Calls"," "},
			{"Unsupported Functions"," "}
	};           

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}	

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value.toString();
		fireTableCellUpdated(row, col);
	}	

}

//Trying to color
class ColorTable extends JTable
{
	boolean newitem[][];
	ColorTable(int row, int col)
	{
		super(row,col);
		newitem= new boolean[row][col];
	}

	public void setValueAt(Object aValue, int row, int column,boolean isnew)
	{
		super.setValueAt(aValue,row,column);
		newitem[row][column]=isnew;
	}

	public void updateUI()
	{
		super.updateUI();
		int columnCount = getColumnCount();
		TableColumnModel columnModel= getColumnModel();
		for(int i=0; i<columnCount; i++)
		{
			columnModel.getColumn(i).setCellRenderer(new ColorCellRenderer());
		}
	}
}


class ColorCellRenderer extends DefaultTableCellRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		ColorTable ct=(ColorTable)table;
		if(ct.newitem[row][column])
		{
			setBackground(Color.cyan);
		}
		else
		{
			setBackground(Color.white);
		}
		return this;
	}
}


//End Ashwini

//Jas
class MemMapPanel extends JPanel {
	int psize_h, psize_w;
	Image pImage;
	int[]pPixels;
	float[] pixels;
	MemoryImageSource source;

	float x_block;
	float y_block;

	private static final Color[] temperatures = new Color[] { Color.WHITE,
		new Color(186, 0, 226), new Color(0, 131, 242),
		new Color(197, 226, 0), new Color(246, 255, 0),
		new Color(226, 112, 0), new Color(255, 0, 0), Color.RED };

	public MemMapPanel() {		
		psize_h = 256;
		psize_w = 256;
		pPixels = new int[psize_w * psize_h + 1];
		pixels = new float[256*256];
		source = new MemoryImageSource (psize_w,psize_h,pPixels,0,psize_w);
		source.setFullBufferUpdates(true);
		source.setAnimated (true);
		pImage = createImage (source);
	}

	private Color colorPicker(float temperature) {
		// Range = -10 to 100
		double scalar = (10.0 + temperature) / 110.0;
		if (scalar < 0) {
			scalar = 0;
		} 
		else if (scalar >= 1) {
			return temperatures[temperatures.length - 1];
		}

		double value = (scalar * (temperatures.length - 1));
		int index = (int) Math.floor(value);

		// Pick the two appropriate colors to lerp
		Color left = temperatures[index];
		Color right = temperatures[index + 1];
		double a = value - index;
		double b = lerp(left.getBlue(), right.getBlue(), a);
		double g = lerp(left.getGreen(), right.getGreen(), a);
		double r = lerp(left.getRed(), right.getRed(), a);
		return new Color((int) r, (int) g, (int) b);
	}

	private double lerp(int left, int right, double amount) {
		return (left + (right - left) * amount);
	}

	public void paintComponent (Graphics g) {
		super.paintComponent (g);

		/*for(int i =0;i<16;i++) {
			Color c = colorPicker(pixels[i*16]);
			g.setColor(c);
			g.fillRect(0,i*16,256,16);*/

		for(int i =0;i<256;i++) {
			Color c = colorPicker(pixels[i]);
			g.setColor(c);
			g.fillRect(0,i,256,1);
		}
	}

	public void pixelFill(float [] pixelValues, float x_blocksize, float y_blocksize) {
		pixels = pixelValues;
		repaint();
	}

	public void pixelFill_old(int [] pixelValues) {
		int index=0;
		int i,j,k,l;
		int red, green=1, blue=1, alpha=255;
		for(k=0;k<256;k++) {
			for(l=0;l<256;l++) {
				red = pixelValues[index] ;
				green = 1 ;
				blue = 1 ;
				//System.out.println("Graphview.java :"+"Updating screen with : [" + red + ", " + green + ", " + blue + "]");
				pPixels[index++] = (255 << alpha) | (red << 16) | (green << 8) | blue;
			}
		}
		source.newPixels (0, 0, psize_w, psize_h);
		repaint ();

	}
}
//End Jas

public class GraphView extends JPanel {

	//Jas
	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";
	private LabelRenderer m_nodeRenderer;
	private EdgeRenderer m_edgeRenderer;
	public static VisualItem curItem ;
	public static String curFuncName="main";
	public static float[] pixelValues;
	public static String WorkLoad;
	public static MyTree td;
	//End Jas

	//Ashwini
	//File Path Declarations
	private static String dir;
	private static String loads_file;
	private static String dyn_xml_file;
	private static String stat_xml_file;
	private static String aliasFolder;
	private static String LibUtilFile;
	private static String partnsXML;
	private static String partnsCode;
	private static String utilsFolder;
	private static String callSequenceFile;
	private static String SupportedFunctionsFile;

	private static Visualization m_vis;
	private Visualization m_vis1;
	private static GraphView m_view;
	private static int threshold=3000;    
	private static String[] CallSequence = new String[2000]; 
	private static HashMap CallSeq = new HashMap();
	public static final int[] palette =new int[] {
		ColorLib.rgb(255,150,150), ColorLib.rgb(150,255,200), 	};;
		public static JSearchPanel searchPanel;
		public static TupleSet callSearch;
		public double codesize, stacksize, rank, brfraction, avloops, mallocsize,calls, usedcodesize,codefraction,unsupported;
		private static SearchTupleSet m_searcher;
		public static JTable funcTable;
		public static JTable aaTable;
		public static String[] columnNames = {"Name","Total","Called","%Util Ratio"};
		public static String[] aliasAnalysisAlgos = { "Anders", "Steens"};
		public static String[] aliasResponses ={"NoAlias","MayAlias","MustAlias"};
		public static DefaultTableModel aamodel;
		public static DefaultListModel listModel;
		public static Object [] fila;
		public static JLabel aaFuncName;
		public static MyComboBox algosComboBox;
		public static MyComboBox aatypeComboBox;
		public static JTabbedPane graphTabbedPane;
		public static JTabbedPane functionTabbedPane;
		public static JPanel CFGPanel;
		public static JLabel warningLabel;
		public static JForcePanel fpanel1;
		public static int selectedTab = 0;
		public static CollapsedSubtreeLayout subLayout;
		private static int currentFuncIndex = 0;
		private static JTextArea output;

		static final MemMapPanel mempanel  = new MemMapPanel();
		private static boolean EnableParititioner = false;

		static final Border etchedBdr = BorderFactory.createEtchedBorder();
		static final Border titledBdr1 = BorderFactory.createTitledBorder(etchedBdr, "Library Utilization Ratio",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr2 = BorderFactory.createTitledBorder(etchedBdr, "Metrics",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr3 = BorderFactory.createTitledBorder(etchedBdr, "Spatial Locality",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr5 = BorderFactory.createTitledBorder(etchedBdr, "Dynamic Call Graph",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		//static final Border titledBdr6 = BorderFactory.createTitledBorder(etchedBdr, "View Call Sequence",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr7 = BorderFactory.createTitledBorder(etchedBdr, "External Functions Not Supported",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr8 = BorderFactory.createTitledBorder(etchedBdr, "Function Search",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr9 = BorderFactory.createTitledBorder(etchedBdr, "Control Flow Graph",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));
		static final Border titledBdr11 = BorderFactory.createTitledBorder(etchedBdr, "Source Tree",TitledBorder.LEFT, TitledBorder.TOP,new Font("arial",Font.BOLD,14));

		static final Border emptyBdr  = BorderFactory.createEmptyBorder(10,10,10,10);
		static final Border compoundBdr1=BorderFactory.createCompoundBorder(titledBdr1, emptyBdr);
		static final Border compoundBdr2=BorderFactory.createCompoundBorder(titledBdr2, emptyBdr);
		static final Border compoundBdr3=BorderFactory.createCompoundBorder(titledBdr3, emptyBdr);
		static final Border compoundBdr5=BorderFactory.createCompoundBorder(titledBdr5, emptyBdr);
		//static final Border compoundBdr6=BorderFactory.createCompoundBorder(titledBdr6, emptyBdr);
		static final Border compoundBdr7=BorderFactory.createCompoundBorder(titledBdr7, emptyBdr);
		static final Border compoundBdr8=BorderFactory.createCompoundBorder(titledBdr8, emptyBdr);
		static final Border compoundBdr9=BorderFactory.createCompoundBorder(titledBdr9, emptyBdr);
		static final Border compoundBdr11=BorderFactory.createCompoundBorder(titledBdr11, emptyBdr);

		Display display;
		Display display1;
		//End Ashwini


		//Jas : Function to Update Memory Map
		public void UpdateMemoryMap()  {
			try
			{
				//System.out.println("In Update Memory Map");
				//Reading Parsed_loads.dmp
				String filename = new String(loads_file+"/LoadsProfile/ld."+curFuncName);
				BufferedReader in = new BufferedReader(new FileReader(filename));
				String loads_str = new String();
				//		String min_str = new String();
				//		String max_str = new String();
				String str = new String();
				String ld_addr = new String();
				String ld_metric = new String();
				String patternString = new String();
				BigInteger min_bi = null;
				BigInteger max_bi = null;
				//patternString = "(" + curFuncName + ")" + ";(\\d+);(\\d+);(.+)";
				patternString = "(\\d+)";
				//patternString = "(\\d+);(\\d+);(.+)";
				Pattern regexp = Pattern.compile(patternString);
				Matcher matcher = regexp.matcher("");
				boolean found =false;
				BigInteger temp = new BigInteger("1");

				//		System.out.println("patternString = "+patternString);
				//Searching for the function specified
				while ((str = in.readLine()) != null)
				{
					//System.out.println("Inside while loop str= "+str);
					matcher.reset( str );
					if( matcher.find() )
					{
						//min_str = matcher.group(2);
						//max_str = matcher.group(3);
						if(min_bi == null)
						{
							//System.out.println("Inside if");
							min_bi = new BigInteger(matcher.group(1));				
							max_bi = new BigInteger(matcher.group(1));					
							//System.out.println("Inside if - end");
						}
						else
						{
							//System.out.println("Inside else");
							temp = new BigInteger(matcher.group(1));				
							max_bi = max_bi.max(temp);
							min_bi = min_bi.min(temp);
							//System.out.println("Inside else -end");

						}
						//				System.out.println("Before loades				
						loads_str = loads_str + matcher.group(1) + ":";
						//				System.out.println("Graphview.java :"+"Found - [" + matcher.group(2) + ", " + max_bi.intValue() + ", " + min_bi.intValue() + temp.intValue() + "]");
						found=true;
					}
				}

				if(!found)
				{
					//		System.out.println("Graphview.java :"+"Number of Loads :0 ");
					return;
				}
				else
					//	System.out.println("Graphview.java :"+"Loads found ");

					for(int i=0;i<256;i++)
					{
						pixelValues[i] = 0.0f;
					}

				int num_loads=0;
				int snap_limit = 65536;
				int min_ld=0;
				int max_ld=0;
				//		min_str.trim();
				//		max_str.trim();
				//		System.out.println("Graphview.java :"+"Going to parse [" + min_str + "]" + " [" + max_str + "]");
				//		BigInteger min_bi = new BigInteger(min_str);
				//		BigInteger max_bi = new BigInteger(max_str);
				BigInteger diff_bi = new BigInteger("1");
				diff_bi = max_bi.subtract(min_bi);
				BigInteger scaled_offset = new BigInteger("65536");
				float scaled_range = 65535.0f;
				//		System.out.println("Graphview.java :"+"Scaling factor is: " +	scaled_range + "  ,  diff is : " + diff_bi.intValue());
				String ld_pat = new String();
				//ld_pat = "(\\d+):([\\d+\\.]+)\\s*";
				ld_pat = "(\\d+):";
				Pattern regexp_inner = Pattern.compile(ld_pat);
				Matcher matcher_inner = regexp_inner.matcher(loads_str);

				while(matcher_inner.find())
				{
					ld_addr = matcher_inner.group(1);
					ld_addr.trim();
					//ld_metric = matcher_inner.group(2);
					//ld_metric = 0.00199005;
					num_loads +=1;
					//			float ld_metric_float = Float.parseFloat(ld_metric);
					float ld_metric_float = (float)0.00199005;
					float scaled_metric = (ld_metric_float * 100);
					BigInteger ld_addr_bi;
					if(!ld_addr.equals(""))
						ld_addr_bi = new BigInteger(ld_addr);
					else
						ld_addr_bi = new BigInteger("9999999");

					ld_addr_bi = ld_addr_bi.subtract(min_bi);
					float ld_addr_int = ld_addr_bi.floatValue() / diff_bi.floatValue();
					float final_offset = (scaled_range * ld_addr_int);
					//System.out.println("Graphview.java :"+"Load :::: " + "[" + final_offset + ", " + scaled_metric + "]" );
					pixelValues[((int)final_offset)%256] += scaled_metric;
				}

				//Filling memory panel

				mempanel.pixelFill(pixelValues, 0.0f, 0.0f);
				//System.out.println("Graphview.java :"+"Number of loads : " + num_loads);
				in.close();
			}
			catch(FileNotFoundException fe)
			{
				System.out.println("Graphview.java :"+"FileNotFoundException in process_hit()");
			}
			catch( IOException e)
			{
				System.out.println("Graphview.java :"+"IOException in process_hit()");
			}
			catch(RuntimeException e)
			{
				System.out.println("Graphview.java :"+"RunTimeException in UpdateMemoryMap()");
				System.out.println("Reason= "+e.getMessage());
			}
		}


		public  void UpdateFunctionTable_Hover(VisualItem item) {
			try{
				funcTable.setValueAt(item.getString("codesize"),0,1);	

				if(Integer.parseInt(item.getString("calls"))!=0)
				{
					funcTable.setValueAt(item.getString("codesize"),1,1);
				}
				else 
				{
					funcTable.setValueAt("0",1,1);
				}
				funcTable.setValueAt(codefraction,2,1);
				//funcTable.setValueAt(item.getString("codefraction"),2,1);
				funcTable.setValueAt(item.getString("stacksize"),3,1);
				funcTable.setValueAt(item.getString("brfraction"),4,1);
				funcTable.setValueAt(item.getString("avloops"),5,1);
				funcTable.setValueAt(item.getString("mallocsize"),6,1);	
				funcTable.setValueAt(item.getString("calls"),7,1);

				if(item.canGetString("hasunsupported"))
				{
					funcTable.setValueAt(item.getString("hasunsupported"),8,1);
				}

				//funcTable.setValueAt(item.getString("rank"),8,1);		
			}
			catch(RuntimeException e)
			{
				//System.out.println("Graphview.java :"+"Runtime Exception in UpdateFunctionTable_Hover()");
			}
		}	



		//Ashwini: Function to Update the Function Characteristics Table
		public void UpdateFunctionTable() {
			try{
				funcTable.setValueAt(codesize,0,1);
				funcTable.setValueAt(usedcodesize,1,1);	
				funcTable.setValueAt(codefraction,2,1);
				funcTable.setValueAt(stacksize,3,1);
				funcTable.setValueAt(brfraction,4,1);
				funcTable.setValueAt(avloops,5,1);
				funcTable.setValueAt(mallocsize,6,1);	
				funcTable.setValueAt(calls,7,1);
				funcTable.setValueAt(unsupported,8,1);

				//funcTable.setValueAt(rank,8,1);		
			}
			catch(RuntimeException e)
			{
				System.out.println("Graphview.java :"+"Runtime Exception in Update FunctionTable()");
			}
		}	

		//Ashwini: Function to Update the Alias Analysis Table
		public void UpdateAATable()
		{
			try
			{
				aamodel.setRowCount(0);
				FileReader input = new FileReader(LibUtilFile);
				BufferedReader bufRead = new BufferedReader(input);
				String line;
				int rowcount = 0;
				while ((line = bufRead.readLine()) != null)
				{				
					String[] tokens =null;
					tokens= line.split(" ");
					aamodel.addRow ( fila ); 
					aamodel.setValueAt (tokens[0],rowcount,0);
					aamodel.setValueAt (tokens[1],rowcount,1);
					aamodel.setValueAt (tokens[2],rowcount,2);
					aamodel.setValueAt (tokens[3],rowcount,3);
					rowcount++;
				}
				bufRead.close();
			}

			catch (IOException e){     
				//	System.out.println("Graphview.java :"+"IO Exception in Function UpdateAATable()");    	             
			}
			catch(RuntimeException e){
				//	System.out.println("Graphview.java :"+"Run Time Exception in Function UpdateAATable()");    	             ;
			}
//				line = bufRead.readLine();
//				int ind = curFuncName.indexOf(".",0);
//				//truncFuncName = curFuncName.subSequence(0, ind).toString();
//				truncFuncName = curFuncName;
				
//
//				while(!line.contains("Function: "+truncFuncName))
//				{
//					line=bufRead.readLine();
//				}
//
//				aaFuncName.setText(line+"\n");
//				int cnt=0;
//				do{				
//					line = bufRead.readLine();
//					if(line.contains("Function")){
//						break;
//					}
//					else if(line.contains(aliasType)) {
//						tokens= line.split("\\,");
//						aamodel.addRow ( fila ); 
//						aamodel.setValueAt ("Lib",0,0);
//						aamodel.setValueAt ("4",0,1);
//						aamodel.setValueAt ("2",0,2);
//						aamodel.setValueAt ("2",0,3);
//						cnt++;
//					}
//					else {
//						continue;
//					}
//
//				}while(!line.contains("Function"));
//				bufRead.close();
//			}
//
//			catch (IOException e){     
//				//	System.out.println("Graphview.java :"+"IO Exception in Function UpdateAATable()");    	             
//			}
//			catch(RuntimeException e){
//				//	System.out.println("Graphview.java :"+"Run Time Exception in Function UpdateAATable()");    	             ;
//			}

		}

		//Ashwini: Function to Read the Dynamic Call Graph Sequence from file
		public static void ReadCallSequence()
		{
			try {
				FileReader input1 = new FileReader(callSequenceFile);
				BufferedReader bufRead = new BufferedReader(input1);
				String line;   
				int i=0;                  	              	             
				line = bufRead.readLine();
				String sofar =" ";
				CallSequence[i]= line;
				while(line!=null){
					i++;
					line=bufRead.readLine();
					sofar+=line;
					CallSequence[i]= line;
					CallSeq.put(line,sofar);
				}
			}
			catch (IOException e){       
				//System.out.println("Graphview.java :"+"IO Exception in Function ReadCallSequence()");  	             
			}
			catch(RuntimeException e){
				//	System.out.println("Graphview.java :"+"Runtime Exception in Function ReadCallSequence()");  	  
			}
		}

		//Ashwini: Function to Reset Values
		public void resetValues()
		{
			codefraction=0.0;
			usedcodesize=0.0;
			codesize=0.0;
			stacksize=0.0;
			brfraction=0.0;
			avloops=0.0;
			mallocsize=0.0;
			unsupported =0.0;
			//rank=0;
			calls=0.0;
		}

		//To truncate double
		private static double truncate(double x)
		{
			long y=(long)(x*1000);
			return (double)y/1000;
		}

		//Ashwini : Get Aggregate Values when multiple Nodes are selected 
		public void GetAggregateValues(TupleSet ts,Tuple[] add, Tuple rem[])
		{
			resetValues();
			int numberOfFuncs = ts.getTupleCount();
			//System.out.println("Graphview.java :Considering the following " +numberOfFuncs+ " functions now");
			Iterator iter = ts.tuples();
			VisualItem ite;

			while(iter.hasNext())
			{
				ite = (VisualItem)iter.next();
				//System.out.println(ite.getString("name"));
				Double thiscode = 0.0;

				if(ite.canGetString("codesize"))
				{
					try
					{
						if(ite.getString("codesize").equals("(NotFound)"))
						{ }
						else
						{
							codesize+= Double.valueOf(ite.getString("codesize")).doubleValue();
							thiscode = Double.valueOf(ite.getString("codesize")).doubleValue();
						}
					}
					catch (RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() codesize");
					}
				}

				if(ite.canGetString("stacksize"))
				{
					try
					{
						stacksize += Double.valueOf(ite.getString("stacksize")).doubleValue();			
					}
					catch (RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() stacksize");
					}
				}

				if(ite.canGetString("brfraction"))
				{
					try
					{
						brfraction+= Double.valueOf(ite.getString("brfraction")).doubleValue();
						brfraction= truncate(brfraction);
					}
					catch(RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() brfraction");
					}
				}

				if(ite.canGetString("avloops"))
				{
					try{
						avloops+= Double.valueOf(ite.getString("avloops")).doubleValue();}
					catch(RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() avloops");
					}
				}
				if(ite.canGetString("mallocsize"))
				{
					try{
						mallocsize+= Double.valueOf(ite.getString("mallocsize")).doubleValue();}
					catch(RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() mallocsize");
					}
				}
				if(ite.canGetString("calls"))
				{

					try{
						int a = Integer.parseInt(ite.getString("calls"));
						calls+= Double.valueOf(ite.getString("calls")).doubleValue();

						if(!ite.getString("calls").equals("0"))
						{
							usedcodesize+= thiscode;
						}
					}
					catch(RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() calls");
					}
				}
				if(ite.canGetString("hasunsupported"))
				{
					try{
						unsupported+= Double.valueOf(ite.getString("hasunsupported")).doubleValue();}
					catch(RuntimeException r)
					{
						//System.out.println("Run Time Exception in GetAggregateValues() mallocsize");
					}
				}
				codefraction = usedcodesize/codesize;
				codefraction = truncate(codefraction);
			}

			//System.out.println("Graphview.java :"+ "CodeSize: "+codesize + " UsedCodeSize :"+usedcodesize+" Code Utilizn : "+codefraction+ " StackSize:"+stacksize +" BrFraction:"+brfraction+" AVL:"+avloops+" Mallocs:"+ mallocsize+  "\n");

		}

		//Ashwini: Function to set values
		public void setValues(VisualItem item)
		{
			Double d =0.0;
			if(item.canGetString("codesize"))
			{
				if(item.getString("codesize").equals("(NotFound)"))
					codesize= 0;
				else
					codesize= Double.valueOf(item.getString("codesize")).doubleValue();
			}
			if(item.canGetString("stacksize"))
			{
				try{
					d = Double.valueOf(item.getString("stacksize")).doubleValue();			
					stacksize= d;
				}
				catch (RuntimeException r)
				{
					stacksize= 0;
				}
			}
			if(item.canGetString("brfraction"))
			{
				try
				{
					brfraction= Double.valueOf(item.getString("brfraction")).doubleValue();
					brfraction = truncate(brfraction);
				}
				catch(RuntimeException r)
				{
					brfraction=0;
				}
			}
			if(item.canGetString("avloops"))
			{
				try
				{
					avloops= Double.valueOf(item.getString("avloops")).doubleValue();
				}
				catch(RuntimeException r)
				{
					avloops=0;
				}
			}
			if(item.canGetString("mallocsize"))
			{
				try
				{
					mallocsize= Double.valueOf(item.getString("mallocsize")).doubleValue();
				}
				catch(RuntimeException r)
				{
					mallocsize=0;
				}
			}
			if(item.canGetString("calls"))
			{
				try{
					calls+= Double.valueOf(item.getString("calls")).doubleValue();}
				catch(RuntimeException r)
				{
					calls=0;
				}
			}
			/*if(item.canGetString("rank"))
		{
			try{
			rank= Double.valueOf(item.getString("rank")).doubleValue();}
			catch(RuntimeException r){
				rank=0;
			}
		}*/
		}

		//Start of Class GraphView
		public GraphView(Graph g, String label) {

			td = new MyTree(WorkLoad, searchPanel);
			curFuncName = new String(); 
			// create a new, empty visualization for our data
			m_vis = new Visualization();

			LabelRenderer nodeR = new LabelRenderer(); 
			nodeR.setRoundedCorner(8,8);
			nodeR.setTextField("name"); 
			EdgeRenderer edgeR = new EdgeRenderer(); 
			//m_vis.setRendererFactory(new DefaultRendererFactory(nodeR, edgeR));

			m_nodeRenderer = new LabelRenderer(label);
			m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
			m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
			m_nodeRenderer.setRoundedCorner(8,8);
			m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);


			DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
			rf.add(new InGroupPredicate(edges), m_edgeRenderer);
			m_vis.setRendererFactory(rf);

//			System.out.println("before setGraph");
			setGraph(g, label);
			//m_vis.add(graph, g);
			// fix selected focus nodes
			TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS); 
			focusGroup.addTupleSetListener(new TupleSetListener() {
				public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem){
					//Update only if we are in the static call graph panel
					if(selectedTab == 0)
					{
						//System.out.println("Graphview.java : Updating function characteristics \n");
						GetAggregateValues(ts,add,rem);
						UpdateFunctionTable();

						for ( int i=0; i<rem.length; ++i )
						{
							((VisualItem)rem[i]).setFixed(false);

						}
						for ( int i=0; i<add.length; ++i ) {
							((VisualItem)add[i]).setFixed(false);
							((VisualItem)add[i]).setFixed(true);
						}
						if ( ts.getTupleCount() == 0 ) {
							ts.addTuple(rem[0]);
							((VisualItem)rem[0]).setFixed(false);
						}
						m_vis.run("draw");
					}
					else
					{
						//Do nothing

					}
				}
			});



			// create actions to process the visual data
			// ActionList filter1 = new ActionList();
			final GraphDistanceFilter filter = new GraphDistanceFilter(graph, 30);

			final int[] shapes = new int[]
			                             { Constants.SHAPE_RECTANGLE, Constants.SHAPE_DIAMOND };

			//  final ShapeAction shape = new DataShapeAction(nodes, "external", shapes);
			//final DataSizeAction size = new DataSizeAction(nodes, "codesize") ;

			// colors
			ItemAction nodeColor = new NodeColorAction(nodes);
			ItemAction textColor = new ColorAction(nodes,VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0));

			subLayout =   new CollapsedSubtreeLayout(nodes);
			m_vis.putAction("subLayout", subLayout);
			m_vis.putAction("textColor", textColor);

			ItemAction edgeColor = new ColorAction(edges,
					VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));

			//quick repaint
			ActionList repaint = new ActionList();
			repaint.add(nodeColor);
			repaint.add(textColor);
			repaint.add(new RepaintAction());
			m_vis.putAction("repaint", repaint);

			//full paint
			ActionList fullPaint = new ActionList();
			fullPaint.add(nodeColor);
			m_vis.putAction("fullPaint", fullPaint);

			//animate paint change
			ActionList animatePaint = new ActionList(400);
			animatePaint.add(new ColorAnimator(nodes));
			animatePaint.add(nodeColor);
			//animate = new ActionList(1000);
			animatePaint.add(new QualityControlAnimator());
			//animate.add(new VisibilityAnimator(tree));
			animatePaint.add(new LocationAnimator(nodes));
			animatePaint.add(new RepaintAction());
			m_vis.putAction("animatePaint", animatePaint);
			//filter1.add(nodeColor);

			//create the filtering and layout
			ActionList filter1 = new ActionList();
			//filter1.add(new FisheyeTreeFilter(tree, 2));
			filter1.add(new FontAction(nodes, FontLib.getFont("Tahoma", 16)));
			//filter1.add(treeLayout);
			//filter1.add(subLayout);
			filter1.add(textColor);
			filter1.add(nodeColor);
			filter1.add(edgeColor);
			m_vis.putAction("filter", filter1);

			subLayout =   new CollapsedSubtreeLayout(nodes);
			m_vis.putAction("subLayout", subLayout);

			ActionList draw = new ActionList();

			draw.add(filter1);
			//draw.add(shape);
			//draw.add(fill);
			draw.add(nodeColor);
			draw.add(textColor);
			//draw.add(size);
			draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 100));
			draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(100,0,0)));
			draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.gray(200)));
			draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.gray(200)));
			draw.add(new RepaintAction());

			ActionList animate = new ActionList(Activity.INFINITY);
			animate.add(new ForceDirectedLayout(graph));
			//animate.add(fill);
			animate.add(nodeColor);
			animate.add(new ColorAnimator(nodes));
			animate.add(textColor);
			//animate.add(shape);
			//animate.add(size);
			animate.add(new RepaintAction());

			m_vis.putAction("animate", animate);
			m_vis.alwaysRunAfter("filter1", "animate");
			m_vis.putAction("draw", draw);
			m_vis.putAction("layout", animate);
			m_vis.runAfter("draw", "layout");
			m_vis.run("filter1");

			TupleSet search = new PrefixSearchTupleSet(); 
			m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);


			search.addTupleSetListener(new TupleSetListener() {
				public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
					m_vis.cancel("animatePaint");
					m_vis.run("fullPaint");
					m_vis.run("animatePaint");
				}
			});

			//ForcePanel
			ForceSimulator fsim = ((ForceDirectedLayout)animate.get(0)).getForceSimulator();
			Force[] forces = fsim.getForces();

			//NBody Force
			//forces[0].setParameter(0,0);
			//Drag Force
			forces[1].setParameter(0,(float)0.025);
			//Spring Force
			forces[2].setParameter(0,(float)1E-5f);
			forces[2].setParameter(1,(float)30);

			fpanel1 = new JForcePanel(fsim);

			//FROM HERE :This has to go from here eventually

			display = new Display(m_vis);
			display1 = new Display(m_vis1);


			//display.setSize(1200,1000);
			display.pan(350, 350);
			display.setForeground(Color.GRAY);
			display.setBackground(Color.WHITE);

			// main display controls
			display.addControlListener(new FocusControl(1));
			display.addControlListener(new DragControl());
			display.addControlListener(new PanControl());
			//display.addControlListener(new ZoomControl());
			display.addControlListener(new WheelZoomControl());
			//display.addControlListener(new ZoomToFitControl());
			display.addControlListener(new NeighborHighlightControl());
			//display.addControlListener(hoverc);

			
			//Start Hover Code
			final Control hoverc = new ControlAdapter(){
				final String group = "name";

				public void itemEntered(VisualItem item, MouseEvent evt) {

					curFuncName = item.getString("name");
					//System.out.println("Graphview.java :"+"Current Function is: "+curFuncName);

					curItem = item;
					resetValues();
					setValues(item);

					//Memory Map Panel is only updated for the Dynamic Call Graph
					if(selectedTab==1)
					{
						UpdateMemoryMap();
					}
					//Alias Analysis Panel is only updated for the Static Call Graph
					//if(selectedTab ==0)
					{
						UpdateAATable();
					}

					//Update Function Characteristics Table
					//GetAggregateValues(ts,add,rem);
					UpdateFunctionTable_Hover(item);
				}

				//Item Exited		
				public void itemExited(VisualItem item, MouseEvent evt) {
				}

				public void itemPressed(VisualItem item, MouseEvent e) {

					if(SwingUtilities.isLeftMouseButton(e))
					{
						curFuncName = item.getString("name");
						//System.out.println("Graphview.java :"+"Selected Function is: "+curFuncName);

						String fn ;
						if(curFuncName.contains("."))
						{
							int ind = curFuncName.indexOf(".",0);
							fn = curFuncName.substring(0, ind);
							//System.out.println("Truncated to :" +fn);

						}
						else
						{
							fn= curFuncName;

						} 
						TreePath treepath = (TreePath)td.trip.get(fn);
						//System.out.println("TreePath is :"+ treepath);
						td.tree.setSelectionPath(treepath);
						td.tree.scrollPathToVisible(treepath);


					}
					if (SwingUtilities.isRightMouseButton(e))
					{
						if(selectedTab ==0) 
						{

							String fn ;
							if(curFuncName.contains("."))
							{
								int ind = curFuncName.indexOf(".",0);
								fn = curFuncName.substring(0, ind);
								//System.out.println("Truncated to :" +fn);

							}
							else
							{
								fn= curFuncName;

							} 

							String funcPath = (String)td.sourcefiles.get(fn);
							MyPopupMenu p = new MyPopupMenu(e, m_vis, display, item, curFuncName,  WorkLoad,funcPath);
							p.createPopupMenu(e);
						}
						if(selectedTab==1)
						{
							DynamicPopupMenu dp = new DynamicPopupMenu(e,m_vis,display, item, curFuncName, WorkLoad);
							dp.createPopupMenu(e);
						}
					}
					else{
						return;
					}
				}	
			}

			;//End of Hover Code

			display.addControlListener(hoverc);

			draw = new ActionList();
			draw.add(filter);
			draw.add(nodeColor);
			draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 100));
			draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(100,0,0)));
			draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.gray(200)));
			draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.gray(200)));
			draw.add(new RepaintAction());

			animate = new ActionList(Activity.INFINITY);
			animate.add(new ForceDirectedLayout(graph));
			animate.add(textColor);
			//animate.add(fill);
			animate.add(nodeColor);
			animate.add(new ColorAnimator(nodes));
			animate.add(new RepaintAction());
			CollapsedSubtreeLayout subLayout =   new CollapsedSubtreeLayout(nodes);
			m_vis.putAction("subLayout", subLayout);

			m_vis.putAction("draw", draw);
			//m_vis.putAction("layout", animate);
			m_vis.runAfter("draw", "layout");
			m_vis.run("draw");
		}


		//Ashwini: Set Graph Function
		public void setGraph(Graph g, String label) {
			// update labeling
			DefaultRendererFactory drf = (DefaultRendererFactory)m_vis.getRendererFactory();
			((LabelRenderer)drf.getDefaultRenderer()).setTextField(label);

			//System.out.println("Inside set graph");
			// update graph
			m_vis.removeGroup(graph);
			VisualGraph vg = m_vis.addGraph(graph, g);
			m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
			VisualItem f = (VisualItem)vg.getNode(0);
			m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
			f.setFixed(false);
		}



		// Main
		public static void main(String[] args) throws IOException 
		{

			//Main options to display to the user	
			String[] options = new String[2];
			options[0]= "Profile a workload";
			options[1]= "Visualize a workload";

			//List of workloads to visualize
			File OutputDir = new File ("../../../../WorkBench/Output");
			File [] workloads = OutputDir.listFiles();
			String[] workloadnames= new String[workloads.length];

			for(int i=0; i< workloads.length;++i)
			{
				int ind = workloads[i].getName().indexOf("_");
				workloadnames[i] =workloads[i].getName().subSequence(0,ind).toString();
			}


			//List of workloads to profile
			File BenchDir = new File ("../../../../WorkBench/Workloads");
			File [] workloadsProf = BenchDir.listFiles();
			String[] workloadnamesProf= new String[workloadsProf.length];

			for(int i=0; i< workloadsProf.length;++i)
			{
				workloadnamesProf[i] =workloadsProf[i].getName().toString();
			}


			final JLabel name=new JLabel(" ");;

			//Main JFrame
			JFrame frame = new JFrame("GLIMPSES Toolkit ");

			//Display options and get user's choice	
			String optionChosen = ListDialog.showDialog(frame, "What would you like to do?", options, name.getText());

			if(optionChosen== null)
			{
				System.out.println("Please chose a valid option.. quitting");
				System.exit(0);
			}
			if(optionChosen.equals("Profile a workload"))
			{
				String workLoadToProfile= null;
				workLoadToProfile = ListDialog.showDialog(frame, "Select WorkLoad to Profile",workloadnamesProf, name.getText());

				if(workLoadToProfile == null)
				{
					System.out.println("Please choose a valid option ..quitting");
					System.exit(0);
				}
				System.out.println("Glimpses : Running Profiler for Workload "+ workLoadToProfile +"\n");



				File workDir = new File("../../../../WorkBench");
				String terminal = "xterm";

				String cmd =terminal +" -e "+ "./profile-" +workLoadToProfile;

				try
				{
					Process ps = Runtime.getRuntime().exec(cmd, null, workDir); 	

					int i = ps.waitFor();


					ps.destroy();

					if(i == 0)
					{
						System.out.println("Successfully executed profiling script");
					}
					else
					{
						System.out.println("Could not execute profiling script");
					}
					System.exit(0);
				}
				catch(Exception ex)
				{
					//System.out.println("Graphview.java: Exception in profiling a workload");
				}
			}

			//If user chooses to visualize a profiled workload
			if(optionChosen.equals("Visualize a workload"))
			{

				String workLoadToVisualize= null;
				if(workloadnames.length == 0)
				{
					System.out.println("Glimpses : No workloads to visualize, please profile it first");
					System.exit(0);
				}
				workLoadToVisualize = ListDialog.showDialog(frame,"Choose WorkLoad to Visualize",workloadnames, name.getText());	
				WorkLoad = workLoadToVisualize;

				if(WorkLoad == null)
				{
					System.out.println("No workload selected..Quitting");
					System.exit(0);
				}

				System.out.println("Graphview.java : Running Visualizer for Workload "+WorkLoad +"\n");

				UILib.setPlatformLookAndFeel();
				String label = "name";
				pixelValues = new float[256*256];
				for(int i=0;i<256*256; i++) 
				{
					pixelValues[i] = 0.0f;
				}
				//Initialize file paths
				dir = "../../../../WorkBench/Output/"+WorkLoad+"_O/";
				loads_file= dir;
				dyn_xml_file=dir +"DynamicCG.xml";
				stat_xml_file=dir+"StaticCG.xml";
				//aliasFolder =dir+ "AliasAnalysis/Modified/";
				LibUtilFile = dir+"LibUtilRatio";
				utilsFolder ="../../../../WorkBench/Utils";
				//callSequenceFile =dir +"/Call_Sequence.dump";
				//SupportedFunctionsFile =dir+"/NotSupported.dump";


				//Reset Values

				//Read Call Sequence
				//ReadCallSequence();

				//get the dynamic graph
				final Graph dynamicGraph = IOLib.newGetGraphFile(dyn_xml_file);
				final GraphView dynamicGraphView = new GraphView(dynamicGraph, label);
				Display dynamicDisplay = dynamicGraphView.display;

				//dynamic display properties		
				dynamicDisplay.setSize(1200,1000);

				//get the static graph
				final Graph staticGraph = IOLib.newGetGraphFile(stat_xml_file);
				final GraphView staticGraphView = new GraphView(staticGraph, label);
				Display staticDisplay = staticGraphView.display;

				//static display properties		
				staticDisplay.setSize(1200,1000);
				staticDisplay.addControlListener(new NeighborHighlightControl());

				JMenu dataMenu = new JMenu("Node Label");
				dataMenu.add(new OpenGraphAction(dynamicGraphView));

				JMenu helpMenu = new JMenu("Help");
				JMenuItem about = new JMenuItem("About");
				helpMenu.add(about);
				about.addActionListener(new helpAction());
				JMenuItem contents = new JMenuItem("Contents");
				helpMenu.add(contents);
				contents.addActionListener(new helpAction());

				JMenuBar menubar = new JMenuBar();
				menubar.add(dataMenu);	
				menubar.add(helpMenu);

				Container content = frame.getContentPane();
				frame.setJMenuBar(menubar);

				//Ashwini
				//Side Panel for displaying Properties

				JPanel fpanel = new JPanel();
				fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.Y_AXIS));


				mempanel.setPreferredSize(new java.awt.Dimension(256,256));

				funcTable = new JTable(new MyTableModel()) {
					//Implement table cell tool tips.
					public String getToolTipText(MouseEvent e) {
						String tip = null;
						java.awt.Point p = e.getPoint();
						int rowIndex = rowAtPoint(p);
						int colIndex = columnAtPoint(p);
						int realColumnIndex = convertColumnIndexToModel(colIndex);

						tip = "The "+ getValueAt(rowIndex, colIndex) + "is "+ getValueAt(rowIndex,colIndex+1);
						return tip;
					}
				};
				funcTable.setPreferredScrollableViewportSize(new Dimension(256,200));
				funcTable.setFillsViewportHeight(true);
				funcTable.setBackground(Color.WHITE);
				final JScrollPane funcScrollPane = new JScrollPane(funcTable);
				warningLabel =new JLabel(" ");

				aamodel = new DefaultTableModel(columnNames,0);
				aaTable = new JTable(aamodel);
				aaTable.setPreferredScrollableViewportSize(new Dimension(300,300));
				aaTable.setFillsViewportHeight(true);
				aaTable.setBackground(Color.WHITE);
				fila = new Object[4];
				aaFuncName= new JLabel("",JLabel.LEFT);
				final JScrollPane aaScrollPane = new JScrollPane(aaTable);   

				algosComboBox = new MyComboBox(aliasAnalysisAlgos);
				aatypeComboBox =new MyComboBox(aliasResponses);

				
				
				//Text field to enter the threshold value
				NumberFormatter numberFormatter = new NumberFormatter();
				numberFormatter.setValueClass(Integer.class);
				final JFormattedTextField threshTextField= 
					new JFormattedTextField(numberFormatter);

				JLabel FuncLabel = new JLabel("Function Properties");

				//Memory Map Panel
				JPanel mPanel = new JPanel();
				mPanel.setLayout(new BoxLayout(mPanel, BoxLayout.Y_AXIS));
				mPanel.add(mempanel);
				mPanel.setBorder(compoundBdr3);

				//Function Characteristics Panel
				JPanel funcPanel = new JPanel();
				funcPanel.setLayout(new BoxLayout(funcPanel, BoxLayout.Y_AXIS));
				funcPanel.add(funcScrollPane);
				funcPanel.add(warningLabel);
				funcPanel.setBorder(compoundBdr2);

				//Alias Analysis Panel
				final JPanel aaPanel = new JPanel();
        aaPanel.setLayout(new BoxLayout(aaPanel, BoxLayout.Y_AXIS));
        //aaPanel.add(algosComboBox);
        //aaPanel.add(aatypeComboBox);
        aaPanel.add(aaFuncName);
        aaPanel.add(aaScrollPane);
       	aaPanel.setBorder(compoundBdr1);


				//Search panel for the graph view
				//System.out.println("Before Search panel");
				searchPanel = new JSearchPanel(m_vis,nodes,Visualization.SEARCH_ITEMS, label, true, true);
				searchPanel.setShowResultCount(true);
				//searchPanel.setBorder(compoundBdr8);

				// Commented by -Sarang    
				//Step thru Panel to see the dynamic call sequence
				//final JPanel playProgramPanel= new JPanel();
				//final JButton playButton= new JButton("Next");
				//final JButton stopButton= new JButton("Step out");
				//playProgramPanel.add(playButton);
				//playProgramPanel.add(stopButton);
				//playProgramPanel.setBorder(compoundBdr6);

				//final JPanel comparePanel = new JPanel();
				//final JTextField fun1 = new JTextField(8);
				//final JTextField fun2 = new JTextField(8);
				//final JButton compareButton = new JButton("Go!");
				//comparePanel.add(fun1);
				//comparePanel.add(fun2);
				//comparePanel.add(compareButton);
				//comparePanel.setBorder(compoundBdr6);
				//compareButton.addActionListener(new ActionListener(){
				//public void actionPerformed(ActionEvent e1)
				//{

				//		System.out.println("Graphview.java : Comparing calling contexts for "+fun1.getText() +"-->"+fun2.getText());
				//}});

				searchPanel.setQuery("main");

				//playButton.addActionListener(new ActionListener() {
				//	public void actionPerformed(ActionEvent e1) 
				//	{
				//		//callSearch.search("main");
				//		searchPanel.setQuery(CallSequence[currentFuncIndex].toString());
				//		/*TupleSet search = m_vis.getGroup(searchGroup);
				//
				//		try
				//		{
				//			m_searcher = new RegexSearchTupleSet(false);
				//			m_searcher.search(CallSequence[currentFuncIndex].toString());
				//		}
				//		catch (RuntimeException r)
				//		{
				//			System.out.println(CallSequence[currentFuncIndex].toString());
				//		}
				//		int r= m_searcher.getTupleCount();
				//		System.out.println(r+ " matches for "+ CallSequence[currentFuncIndex].toString());
				//		*/
				//		//UpdateFunctionTable();
				//                  			
				//		if(currentFuncIndex==CallSequence.length)
				//		{
				//			return;
				//		}
				//		else
				//		{
				//			currentFuncIndex++;
				//			curFuncName= CallSequence[currentFuncIndex];
				//		}
				//	
				//	}});

				//stopButton.addActionListener(new ActionListener() {
				//public void actionPerformed(ActionEvent e1) 
				//{
				//System.out.println("Graphview.java :"+"Stopping Call Sequence\n");
				//	searchPanel.setQuery("");
				//	while(CallSequence[currentFuncIndex].equals(curFuncName))
				//	{
				//		currentFuncIndex++;
				//	}
				//	searchPanel.setQuery(CallSequence[currentFuncIndex].toString());
				//}});

				//Functions not on SPE

				//String line=" ";
				//BufferedReader in = new BufferedReader(new FileReader(SupportedFunctionsFile));
				listModel = new DefaultListModel();
				//while(line!=null)
				//{
				//	line=in.readLine();
				//	listModel.addElement(line);
				///}
				JList funcList = new JList(listModel);
				JScrollPane notSupportedPane = new JScrollPane(funcList);
				notSupportedPane.setPreferredSize(new Dimension(200,50));
				JPanel notSupportedPanel = new JPanel();
				notSupportedPanel.add(notSupportedPane);
				notSupportedPanel.setBorder(compoundBdr7);

				//Add all components to the main panel
				//fpanel.add(FuncLabel);
				fpanel.add(mPanel);
				fpanel.add(funcPanel);

				//fpanel.add(fpanel1); //Force panel, to manually change the forces
				fpanel.add(aaPanel);
				//fpanel.add(notSupportedPanel);

				mempanel.pixelFill(pixelValues, 0.0f, 0.0f);
				fpanel.add(Box.createVerticalGlue());

				functionTabbedPane = new JTabbedPane();
				functionTabbedPane.addTab("Function Properties", fpanel);

				//Create a JTabbedPane	
				graphTabbedPane = new JTabbedPane();

				//Static Call Graph Display Panel
				JPanel StaticGraphPanel = new JPanel();
				StaticGraphPanel.setLayout(new BoxLayout(StaticGraphPanel, BoxLayout.X_AXIS));
				StaticGraphPanel.add(staticDisplay);
				graphTabbedPane.addTab( "Static Call Graph", StaticGraphPanel);

				//Dynamic Call Graph Display Panel
				JPanel DynamicGraphPanel = new JPanel();
				DynamicGraphPanel.setLayout(new BoxLayout(DynamicGraphPanel, BoxLayout.X_AXIS));
				DynamicGraphPanel.add(dynamicDisplay);
				DynamicGraphPanel.setPreferredSize(new Dimension(1200,800));
				graphTabbedPane.addTab( "Dynamic Call Graph", DynamicGraphPanel);


				// Register a change listener
				graphTabbedPane.addChangeListener(new ChangeListener() 
				{
					public void stateChanged(ChangeEvent evt) {
						JTabbedPane pane = (JTabbedPane)evt.getSource();
						// Get current tab
						selectedTab =pane.getSelectedIndex();

						if(selectedTab == 1)
						{
							//System.out.println("Graphview.java : Dynamic Graph Pane selected \n");	

							//Disable play program panel
							//playProgramPanel.setEnabled(false); - Commented by Sarang
							//playButton.setEnabled(false);
							//stopButton.setEnabled(false);

							//Enable Memory Map Panel
							mempanel.setEnabled(true);

							//Disable Alias Analysis Panel
							aaPanel.setEnabled(true);
							algosComboBox.setEnabled(false);	
							aatypeComboBox.setEnabled(false);
							aaTable.setEnabled(false);	
							aaScrollPane.setEnabled(false);

						}
						if(selectedTab == 0)
						{
							//System.out.println("Graphview.java: Static Graph Pane selected \n");

							//Disable memory map panel
							mempanel.setEnabled(false);

							//Enable play program panel
							//playProgramPanel.setEnabled(true); - Commented by Sarang
							//playButton.setEnabled(true);
							//stopButton.setEnabled(true);

							//Enable Alias Analysis panel	
							aaPanel.setEnabled(true);
							algosComboBox.setEnabled(true);	
							aatypeComboBox.setEnabled(true);
							aaTable.setEnabled(true);
							aaScrollPane.setEnabled(true);	
						}

					}

				});
				JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,graphTabbedPane,functionTabbedPane);
				split.setOneTouchExpandable(false);
				split.setContinuousLayout(false);
				split.setDividerSize(10);
				split.setDividerLocation(310);
				//End Ashwini

				JPanel mainpanel = new JPanel();
				mainpanel.setLayout (new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
				JTextField search = new JTextField("Enter Function Name");
				mainpanel.add(searchPanel);

				//	td = new MyTree(WorkLoad, searchPanel,treePaths);
				mainpanel.add(td);
				//mainpanel.add(playProgramPanel); // Commented by Sarang
				//	mainpanel.add(comparePanel);
				//	mainpanel.setBorder(compoundBdr11);

				JTabbedPane programTabbedPane = new JTabbedPane();
				programTabbedPane.addTab(WorkLoad, mainpanel);

				JSplitPane mainsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,programTabbedPane,split);
				split.setOneTouchExpandable(false);
				split.setContinuousLayout(false);
				split.setDividerSize(10);
				split.setDividerLocation(1110);

				content.add(mainsplit);

				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}//End of main


		public static class helpAction extends AbstractAction
		{
			public helpAction()
			{

			}
			public void actionPerformed(ActionEvent e)
			{
				//Get frame	
				Component c;
				c= m_view;
				while ( c != null && !(c instanceof JFrame) ) {
					c = c.getParent();
				}
				final Component ci = c;

				//Identify which Menu Item was selected
				JMenuItem source = (JMenuItem)(e.getSource());
				String src = source.getText();

				//If Contents was selected
				if(src == "Contents")
				{

					//System.out.println("GraphView.java : Displaying help contents");

					final JDialog helpDialog = new JDialog((JFrame)ci, "Help Contents",true);
					HelpTree helptree =new HelpTree();
					JTextArea help = new JTextArea("License");
					try{
						Reader reader = new FileReader("../../../../README");
						help.read(reader,null);
					}
					catch(Exception ex)
					{
						//System.out.println("Graphview.java: Runtime Exception in help button");
					}	

					JScrollPane scroll = new JScrollPane(help);
					JButton closeButton = new JButton("Close");
					closeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							helpDialog.setVisible(false);
						}
					});
					helpDialog.setContentPane(new HelpTree());
					helpDialog.pack();	
					helpDialog.setLocationRelativeTo(ci);
					helpDialog.setVisible(true);    
					helpDialog.dispose();
				}

				//If About was selected
				if(src == "About")
				{
					final JDialog dialog = new JDialog( (JFrame)ci, "About Glimpses", true);
					JEditorPane content = new JEditorPane("text/html"," ");
					content.setText("<center><h1> Glimpses 0.92 </h1> A profiling tool for understanding program <br/>memory behavior for its suitability to be <br/>executed on the Cell Broadband Engine <br/></center");

					dialog.setVisible(false);
					final JButton Credits = new JButton("Credits");
					Credits.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							final JDialog creditsDialog = new JDialog((JFrame)ci, "Credits",true);
							JEditorPane creditsPane = new JEditorPane("text/html"," ");
							creditsPane.setText("<h2>Developed by</h2> Jaswanth Sreeram <u>jaswanth@cc.gatech.edu</u> <br/> Ashwini Bhagwat <u>ashwini@cc.gatech.edu</u> <br/> <h3>Georgia Institute of Technology </h3>"); 
							JPanel creditsPanel = new JPanel(new BorderLayout());
							JButton closeButton = new JButton("Close");
							closeButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									creditsDialog.setVisible(false);
								}	
							});
							creditsPanel.add(creditsPane,BorderLayout.CENTER);
							creditsPanel.add(closeButton,BorderLayout.SOUTH);
							creditsDialog.setContentPane(creditsPanel);
							creditsDialog.pack();	
							creditsDialog.setLocationRelativeTo(ci);
							creditsDialog.setVisible(true);    
							creditsDialog.dispose();
						}
					});
					JButton License = new JButton("License");
					License.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e)  {
							final JDialog licenseDialog = new JDialog((JFrame)ci, "License",true);
							JTextArea license = new JTextArea("License");
							try{
								Reader reader = new FileReader("../../../../WorkBench/LICENSE.TXT");
								license.read(reader,null);
							}
							catch(Exception ex)
							{
								//System.out.println("Graphview.java: Runtime Exception in licenseButton");
							}	
							JScrollPane scroll = new JScrollPane(license);
							JPanel licensePanel = new JPanel(new BorderLayout());
							JButton closeButton = new JButton("Close");
							closeButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									licenseDialog.setVisible(false);
								}
							});
							licensePanel.add(scroll,BorderLayout.CENTER);
							licensePanel.add(closeButton, BorderLayout.SOUTH);
							licenseDialog.setContentPane(licensePanel);
							licenseDialog.pack();	
							licenseDialog.setLocationRelativeTo(ci);
							licenseDialog.setVisible(true);    
							licenseDialog.dispose();
						}

					});

					JButton Close = new JButton("Close");
					Close.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dialog.setVisible(false);
						}
					});

					// layout the buttons
					Box bbox = new Box(BoxLayout.X_AXIS);
					bbox.add(Box.createHorizontalStrut(5));
					bbox.add(Box.createHorizontalGlue());
					bbox.add(Credits);
					bbox.add(Box.createHorizontalStrut(5));
					bbox.add(License);
					bbox.add(Box.createHorizontalStrut(5));
					bbox.add(Close);
					bbox.add(Box.createHorizontalStrut(5));
					// put everything into a panel
					JPanel panel = new JPanel(new BorderLayout());
					panel.add(content, BorderLayout.CENTER);
					panel.add(bbox, BorderLayout.SOUTH);
					panel.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));

					// show the dialog
					dialog.setContentPane(panel);
					dialog.pack();
					dialog.setLocationRelativeTo(c);
					dialog.setVisible(true);
					dialog.dispose();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

				}
			}
		}
		//Class that creates menu for opening an xml file, and loading the graph
		public static class OpenGraphAction extends AbstractAction
		{
			//private GraphView m_view;
			public OpenGraphAction(GraphView view) 
			{
				m_view = view;
				this.putValue(AbstractAction.NAME, "Choose Label");
				this.putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke("ctrl O"));
			}
			public void actionPerformed(ActionEvent e) 
			{
				Graph g = IOLib.newGetGraphFile(dyn_xml_file);
				if ( g == null ) return;
				String label = getLabel(m_view, g);
				if ( label != null ) {
					System.out.println("Before calling m_view.setGraph");
					m_view.setGraph(g, label);
				}
			}

			//To get the node labels -> name,codesize, stack etc
			public static String getLabel(Component c, Graph g) 
			{
				// get the column names
				Table t = g.getNodeTable();
				int  cc = t.getColumnCount();
				String[] names = new String[cc];
				for ( int i=0; i<cc; ++i )
					names[i] = t.getColumnName(i);

				// where to store the result
				final String[] label = new String[1];

				// -- build the dialog -----
				// we need to get the enclosing frame first
				while ( c != null && !(c instanceof JFrame) ) {
					c = c.getParent();
				}
				final JDialog dialog = new JDialog(
						(JFrame)c, "Choose Label Field", true);

				// create the ok/cancel buttons
				final JButton ok = new JButton("OK");
				ok.setEnabled(false);
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialog.setVisible(false);
					}
				});

				// build the selection list
				final JList list = new JList(names);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.getSelectionModel().addListSelectionListener(
						new ListSelectionListener() {
							public void valueChanged(ListSelectionEvent e) {
								int sel = list.getSelectedIndex(); 
								if ( sel >= 0 ) {
									ok.setEnabled(true);
									label[0] = (String)list.getModel().getElementAt(sel);
								} else {
									ok.setEnabled(false);
									label[0] = null;
								}
							}
						});


				JScrollPane scrollList = new JScrollPane(list);
				JLabel title = new JLabel("Choose a field to use for node labels:");

				// layout the buttons
				Box bbox = new Box(BoxLayout.X_AXIS);
				bbox.add(Box.createHorizontalStrut(5));
				bbox.add(Box.createHorizontalGlue());
				bbox.add(ok);
				bbox.add(Box.createHorizontalStrut(5));
				bbox.add(Box.createHorizontalStrut(5));

				// put everything into a panel
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(title, BorderLayout.NORTH);
				panel.add(scrollList, BorderLayout.CENTER);
				panel.add(bbox, BorderLayout.SOUTH);
				panel.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));

				// show the dialog
				dialog.setContentPane(panel);
				dialog.pack();
				dialog.setLocationRelativeTo(c);
				dialog.setVisible(true);
				dialog.dispose();

				// return the label field selection
				return label[0];
			}
		}//End of get label

		//Overview listener
		public static class FitOverviewListener implements ItemBoundsListener {
			private Rectangle2D m_bounds = new Rectangle2D.Double();
			private Rectangle2D m_temp = new Rectangle2D.Double();
			private double m_d = 15;
			public void itemBoundsChanged(Display d) {
				d.getItemBounds(m_temp);
				GraphicsLib.expand(m_temp, 25/d.getScale());

				double dd = m_d/d.getScale();
				double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
				double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
				double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
				double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
				if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
					m_bounds.setFrame(m_temp);
					DisplayLib.fitViewToBounds(d, m_bounds, 0);
				}
			}
		}

} // end of class GraphView
