import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

/*Puzzle: class representing a triangular puzzle with pieces that are either triangular or diamond-shaped
Each puzzle piece has a single label on each of its sides
A puzzle is valid if every space contains (part of) a puzzle piece (contained entirely within the puzzle)
and adjacent sides have the same label; also, the edges on the outer boundary of a puzzle must be labeled
with either a 0, a 1, or a 2

There are two types of puzzle pieces: normal pieces and equivariant pieces
normal pieces are all triangles, equivariant pieces are all diamonds 
Equivariant pieces are usually depicted as being green 

Triangles are represented as arrays of three strings indicating the labels on the sides 
Diamonds consist of two triangular puzzle pieces, one on top of the other 

Each puzzle has a weight, which is a polynomial in the same number of variables as the number of rows
in the puzzle 

The class also has methods from drawing the puzzle on a Graphic object and drawing certain paths on the puzzle */
class Puzzle {
	//valid puzzle pieces that are not equivariant
	public static String normal_pieces[][]={{"0","0","0"},{"1","1","1"},{"2","2","2"},{"2","0","20"},
	{"0","20","2"},{"20","2","0"},{"1","0","10"},{"0","10","1"},{"10","1","0"},{"2","1","21"},
	{"1","21","2"},{"21","2","1"},{"2","10","2(10)"},{"10","2(10)","2"},{"2(10)","2","10"},
	{"21","0","(21)0"},{"0","(21)0","21"},{"(21)0","21","0"}};
	
	//valid equivariant puzzle pieces
	public static String equivariant_pieces[][][]={{{"0","1","#012"},{"1","#012","0"}},{{"0","2","!012"},{"2","!012","0"}},{{"1","2","?012"},{"2","?012","1"}},
	{{"10","2","*012"},{"2","*012","10"}},{{"0","21","^012"},{"21","^012","0"}},{{"10","21","@012"},{"21","@012","10"}}};
	
	
	String puzzle[];
	int nrows;
	
	//construct a puzzle with n rows
	public Puzzle(int n)
	{
		nrows=n;
		
		puzzle=new String[(3*n*(n+1))/2];
		
		for(int i=0;i<puzzle.length;i++)
		{
			puzzle[i]=new String("E"); //E for empty
		}
	}
	
	//get the ith triangle in the row
	public String[] get_triangle(int row, int i)
	{
		String ret[]=new String[3];
		
		if(row>=nrows)
		{
			return ret;
		}
		else if(i>2*row+1)
		{
			return ret;
		}
		
		//left side
		ret[0]=puzzle[row_start(row)+i];
		
		//even triangle or odd?
		
		if(i%2==0) //even, point is up
		{
			//right side
			ret[1]=puzzle[row_start(row)+i+1];
			//now need the bottom
			
			ret[2]=puzzle[row_bottom_start(row)+i/2];
		}
		else //odd, point is down
		{
			//right side
			ret[2]=puzzle[row_start(row)+i+1];
			//now need the top
			ret[1]=puzzle[row_bottom_start(row-1)+(i-1)/2];
		}
		return ret;
	}
	
	//set the ith triangle in the row
	public void set_triangle(int row, int i,String[] piece)
	{
		
		if(row>=nrows)
		{
			return;
		}
		else if(i>2*row+1)
		{
			return;
		}
		
		//left side
		puzzle[row_start(row)+i]=piece[0];
		
		//even triangle or odd?
		
		if(i%2==0) //even, point is up
		{
			//right side
			puzzle[row_start(row)+i+1]=piece[1];
			//now need the bottom
			
			puzzle[row_bottom_start(row)+i/2]=piece[2];
		}
		else //odd, point is down
		{
			//right side
			puzzle[row_start(row)+i+1]=piece[2];
			//now need the top
			puzzle[row_bottom_start(row-1)+(i-1)/2]=piece[1];
		}
	}
	
	//check if a labeled triangle is a valid normal piece
	public boolean valid_normal_piece(String [] piece)
	{
		for(int i=0;i<normal_pieces.length;i++)
		{
			if(normal_pieces[i][0].equals(piece[0])&&normal_pieces[i][1].equals(piece[1])
			&&normal_pieces[i][2].equals(piece[2]))
				return true;
		}
		return false;
	}
	
	//check if a labeled triangle is a valid equivariant piece
	public boolean valid_equivariant_piece(String[] piece1,String [] piece2)
	{
		for(int i=0;i<equivariant_pieces.length;i++)
		{
			if(equivariant_pieces[i][0][0].equals(piece1[0])&&equivariant_pieces[i][0][1].equals(piece1[1])
			&&equivariant_pieces[i][0][2].equals(piece1[2])&&equivariant_pieces[i][1][0].equals(piece2[0])
			&&equivariant_pieces[i][1][1].equals(piece2[1])&&equivariant_pieces[i][1][2].equals(piece2[2]))
				return true;
		}
		return false;
	}
	
	//index in the puzzle at the beginning of row number "row"
	public int row_start(int row)
	{
		return (3*(row)*(row+1))/2;
	}
	
	//index in the puzzle at the bottom of row number "row
	public int row_bottom_start(int row)
	{
		return row_start(row)+(row+1)*2;
	}
	
	//make a vector of puzzle pieces that fit in the space (row,i) preserving validity
	public Vector<String[]> complete_piece(int row,int i)
	{
		Vector<String[]> ret=new Vector();
		Vector<Integer> filled_spots=new Vector();
		
		String[] piece=get_triangle(row,i);
		
		for(int j=0;j<piece.length;j++)
		{
			if(!piece[j].equals("E"))
				filled_spots.add(new Integer(j));
		}
		
		for(int j=0;j<normal_pieces.length;j++)
		{
			boolean can_use=true;
			for(int k=0;k<filled_spots.size();k++)
			{
				int m=filled_spots.get(k).intValue();
				if(!piece[m].equals(normal_pieces[j][m]))
				{
					can_use=false;
					break;
				}
			}
			if(can_use)
				ret.add(normal_pieces[j]);
		}
		
		if(row<nrows-1||i%2==1) //can't have the top of an equivariant piece in the last row
		{
			for(int j=0;j<equivariant_pieces.length;j++)
			{
				boolean can_use=true;
				for(int k=0;k<filled_spots.size();k++)
				{
					int m=filled_spots.get(k).intValue();
					if(!piece[m].equals(equivariant_pieces[j][i%2][m]))
					{
						can_use=false;
						break;
					}
				}	
				if(can_use)
					ret.add(equivariant_pieces[j][i%2]);
			}
		}
		return ret;
	}
	
	
	//set the boundary of the puzzle, first left (bottom to top), then right (top to bottom), 
	//then bottom (left to right)
	public void setBoundary(String[] left, String[] right, String[] bottom)
	{
		//left
		for(int row=0;row<nrows;row++)
		{
			//set left BACKWARDS
			puzzle[row_start(row)]=left[nrows-1-row];
			//set right FORWARDS
			puzzle[row_bottom_start(row)-1]=right[row];
			//set bottom FORWARDS
			puzzle[row_bottom_start(nrows-1)+row]=bottom[row];
		}
	}
	
	public Puzzle clone()
	{
		Puzzle p=new Puzzle(nrows);
		
		System.arraycopy(puzzle,0,p.puzzle,0,puzzle.length);
		return p;
	}
	
	//an edge label of "E" indicates that the edge is unlabeled. This method checks if a triangle
	//has any unlabeled edges
	public boolean piece_complete(int row,int i)
	{
		String piece[]=get_triangle(row,i);
		
		for(int j=0;j<piece.length;j++)
			if(piece[j].equals("E"))
				return false;
		return true;
	}
	
	public boolean equals(Object o)
	{
		Puzzle p=(Puzzle)o;
		
		for(int i=0;i<puzzle.length;i++)
			if(!puzzle[i].equals(p.puzzle[i]))
				return false;
		return true;
	}
	
	//weight of the puzzle, assuming it is valid
	public String weight()
	{
		String w="";
		boolean didone=false;
		for(int row=0;row<nrows-1;row++)
		{
			for(int i=0;i<2*row+2;i++)
			{
				if(i%2==0&&valid_equivariant_piece(get_triangle(row,i),get_triangle(row+1,i+1)))
				{
					didone=true;
					w+="(t"+(i/2+(nrows-row))+" - t"+(i/2+1)+")";
				}
			}
		}
		if(!didone)
			w="1";
		return w;
	}
	
	public static int triSize=60;
	
	//draw a line between edges of a triangle, with offset
	public void draw_line_between_edges(Graphics g, int x, int y, int row, int i, int e1, int e2)
	{
		int addone=0;
		
		if(i%2==0)
			addone=1;
		int xpos=x+(nrows-row-1)*triSize/2+i*triSize/2;
		int ypos=y+(int)((row+1)*Math.sqrt(3)*triSize/2);
		
		int xpos2=xpos,ypos2=ypos;
		
		if(i%2==0)
		{
			if(e1==2)
			{
				xpos+=triSize/2;
			}
			if(e2==2)
			{
				xpos2+=triSize/2;
			}
			if(e1==1)
			{
				xpos+=3*triSize/4;
				ypos-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e2==1)
			{
				xpos2+=3*triSize/4;
				ypos2-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e1==0)
			{
				xpos+=triSize/4;
				ypos-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e2==0)
			{
				xpos2+=triSize/4;
				ypos2-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
		}
		else
		{
			if(e1==0)
			{
				xpos+=triSize/4;
				ypos-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e2==0)
			{
				xpos2+=triSize/4;
				ypos2-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e1==1)
			{
				xpos+=triSize/2;
				ypos-=(int)(Math.sqrt(3)/2*triSize);
			}
			if(e2==1)
			{
				xpos2+=triSize/2;
				ypos2-=(int)(Math.sqrt(3)/2*triSize);
			}
			if(e1==2)
			{
				xpos+=3*triSize/4;
				ypos-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
			if(e2==2)
			{
				xpos2+=3*triSize/4;
				ypos2-=(int)(Math.sqrt(3)/2*triSize)/2;
			}
		}
		g.drawLine(xpos,ypos,xpos2,ypos2);
		
		g.fillOval(xpos2-3,ypos2-3,6,6);
	}
	
	//draw paths from bottom to top left
	public void draw_paths(Graphics g, int x, int y)
	{
		Vector<Point> cross=new Vector();
		
		
		for(int j=0;j<nrows;j++)
		{
			int entered_from=2;
		
			String tri_j[]=get_triangle(nrows-1,j*2);
			
			
			String label=new String(tri_j[2]);
			
			int row=nrows-1;
			int k=j*2;
			
			
			do
			{
			
			if(row<0||row>=nrows||k>=(row+1)*2||k<0)
				break;
			
			tri_j=get_triangle(row,k);
			
			int lookup[]=new int[3];
			
			if(k%2==0)
			{
				lookup[0]=0;
				lookup[1]=1;
				lookup[2]=2;
			}
			else
			{
				lookup[0]=1;
				lookup[1]=0;
				lookup[2]=2;
			}
			
			boolean didit=false;
			
			int edge=0;
			
			if(k%2==0&&label.equals("1")&&entered_from==1&&tri_j[entered_from].equals("21")&&tri_j[0].equals("10"))
			{
				edge=entered_from;
				entered_from=1;
				didit=true;
				row++;
				k++;
			}
			else if(k%2==1&label.equals("1")&&entered_from==1&&tri_j[0].equals("21")&&tri_j[2].equals("10"))
			{
				edge=entered_from;
				entered_from=1;
				didit=true;
				k--;
			}
			else
			{
			for(int i=0;i<3;i++)
			{
				edge=lookup[i];
				if(edge!=entered_from)
				{
					if(tri_j[edge].indexOf(label)!=-1)
					{
						draw_line_between_edges(g,x,y,row,k,entered_from,edge);
						didit=true;
						
						//left,right,bottom
						if(k%2==0)
						{
							if(entered_from==0)
							{
								if(edge==1)
								{
									k++;
								}
								else if(edge==2)
								{
									row++;
									k++;
								}
							}
							else if(entered_from==1)
							{
								if(edge==0)
									k--;
								else if(edge==2)
								{
									row++;
									k++;
								}
							}
							else if(entered_from==2)
							{
								if(edge==0)
								{
									k--;
								}
								else if(edge==1)
								{
									k++;
								}
							}
						
							if(edge==0)
							{
								entered_from=2;
							}
							else if(edge==1)
							{
								entered_from=0;
								
							}
							else
							{
								entered_from=1;
							}
						}
						else
						{
							//left, top, right
							if(edge==0)
							{
								entered_from=1;
								k--;
							}
							else if(edge==1)
							{
								entered_from=2;
								k--;
								row--;
							}
							else
							{
								entered_from=0;
								k++;
							}
						}
						
						break;
					}
				}
			}
			}
			
			if(!didit)
				break;
				
			
			
			} while (k>=0);
		}
	}
	
	//draw paths from bottom to top right
	public void draw_paths_2(Graphics g, int x, int y)
	{
		
		for(int j=0;j<nrows;j++)
		{
			String tri_j[]=get_triangle(nrows-1,j*2);
			
			int entered_from=2;
		
			
			String label=tri_j[2];
			
			int row=nrows-1;
			int k=j*2;
			
			do
			{
			
			if(row<0||row>=nrows||k>=(row+1)*2-1||k<0)
				break;
			tri_j=get_triangle(row,k);
			
			int lookup[]=new int[3];
			
			if(k%2==0)
			{
				lookup[0]=1;
				lookup[1]=2;
				lookup[2]=0;
			}
			else
			{
				lookup[0]=1;
				lookup[1]=2;
				lookup[2]=0;
			}
			
			boolean didit=false;
			
			if(k%2==0&&label.equals("1")&&entered_from==0&&tri_j[entered_from].equals("10")&&tri_j[1].equals("21"))
			{
				entered_from=1;
				didit=true;
				row++;
				k++;
			}
			else if(k%2==1&label.equals("1")&&entered_from==1&&tri_j[0].equals("21")&&tri_j[2].equals("10"))
			{
				entered_from=0;
				didit=true;
				k++;
			}
			else
			{
			for(int i=0;i<3;i++)
			{
				int edge=lookup[i];
				if(edge!=entered_from)
				{
					if(tri_j[edge].indexOf(label)!=-1)
					{
						draw_line_between_edges(g,x+triSize/10,y-triSize/10,row,k,entered_from,edge);
						didit=true;
						
						//left,right,bottom
						if(k%2==0)
						{
							
							if(edge==0)
							{
								entered_from=2;
								k--;
							}
							else if(edge==1)
							{
								entered_from=0;
								k++;
							}
							else
							{
								entered_from=1;
								k++;
								row++;
							}
						}
						else
						{
							//left, top, right
							if(edge==0)
							{
								entered_from=1;
								k--;
							}
							else if(edge==1)
							{
								entered_from=2;
								k--;
								row--;
							}
							else
							{
								entered_from=0;
								k++;
							}
						}
						
						break;
					}
				}
			}
			}
			
			if(!didit)
			{
				break;
			}	
			
			} while (k<2*row+2);
		}
	}
	
	//draw the puzzle
	public void draw(Graphics g,int x, int y)
	{
		int ybase=0;
		for(int row=0;row<nrows;row++)
		{
			int xpos=x+(nrows-row-1)*triSize/2;;
			ybase=y+(row+1)*((int)(Math.sqrt(3)*triSize/2));
			
			for(int i=0;i<2*row+2;i++)
			{
				if(!puzzle[row_start(row)+i].equals("E"))
				{
				if(i%2==0)
				{
					//line up and to the right
					g.drawLine(xpos,ybase,xpos+triSize/2,ybase-(int)((Math.sqrt(3)/2)*(triSize)));
					
					
					g.drawString(puzzle[row_start(row)+i],xpos+triSize/4,ybase-triSize/4);
				}
				else
				{
					//line down and to the right
					g.drawLine(xpos,ybase-(int)((Math.sqrt(3)/2)*(triSize)),xpos+triSize/2,ybase);
					g.drawString(puzzle[row_start(row)+i],xpos+triSize/4,ybase-triSize/4);
				}
				}
				xpos+=triSize/2;
			}
		
			xpos=x+(nrows-row-1)*triSize/2;;
			
			
			boolean equiv=false;
			
			for(int i=2*row+2;i<3*row+3;i++)
			{
				equiv=false;
			
				if(row<nrows-1)
				{
					
					String[] tri1=get_triangle(row,2*(i-2*row-2)),tri2=get_triangle(row+1,2*(i-2*row-2)+1);
					
					if(valid_equivariant_piece(tri1,tri2))
					{
					equiv=true;
					int xpoints[]={xpos,xpos+triSize/2,xpos+triSize,xpos+triSize/2};
					int ypoints[]={ybase,(int)(ybase+Math.sqrt(3)/2*triSize),ybase,(int)(ybase-Math.sqrt(3)/2*triSize)};
					
					Color old=g.getColor();
					g.setColor(Color.green);
					
					g.fillPolygon(xpoints,ypoints,4);
					
					g.setColor(old);
					g.drawLine(xpoints[0],ypoints[0],xpoints[1],ypoints[1]);
					g.drawLine(xpoints[1],ypoints[1],xpoints[2],ypoints[2]);
					g.drawLine(xpoints[2],ypoints[2],xpoints[3],ypoints[3]);
					g.drawLine(xpoints[3],ypoints[3],xpoints[0],ypoints[0]);
					
					g.drawString(tri1[0],xpos+triSize/4,ybase-triSize/4);
					g.drawString(tri1[1],xpos+3*triSize/4,ybase-triSize/4);
					}
				}
				
				if(!equiv)
				{
					if(!puzzle[row_start(row)+i].equals("E"))
					{
						g.drawLine(xpos,ybase,xpos+triSize,ybase);
						g.drawString(puzzle[row_start(row)+i],xpos+triSize/2,ybase);
					}
				}
				xpos+=triSize;
				
			}
		}
		g.drawString("Weight: "+weight(),x,ybase+triSize/2);
	}
	
	
		
	//check if the piece is a valid part of an equivariant piece
	public boolean valid_equivariant_part(String[] tri,int p)
	{
		for(int i=0;i<equivariant_pieces.length;i++)
		{
			if(equivariant_pieces[i][p%2][0].equals(tri[0])&&equivariant_pieces[i][p%2][1].equals(tri[1])
			&&equivariant_pieces[i][p%2][2].equals(tri[2]))
				return true;
		}
		return false;
	}
	
	//check if the puzzle is valid
	public boolean valid()
	{
		for(int row=0;row<nrows;row++)
		{
			for(int i=0;i<2*row+1;i++)
			{
				String tri[]=get_triangle(row,i);
				
				if(!valid_normal_piece(tri)&&!valid_equivariant_part(tri,i))
					return false;
			}
		}
		return true;
	}
	
	public int hashCode()
	{
		int sum=0;
		for(int i=0;i<puzzle.length;i++)
		{
			sum+=puzzle[i].charAt(0);
		}
		return sum;
	}
}

public class PuzzleGen extends JFrame {

	public static PuzzleGen frame;
	public static int n;
	public static int WIDTH=1200;
	public static int HEIGHT=700;
	public static int numperrow=3;
	public static int vertinc=Puzzle.triSize/2;
	public static int horizinc=Puzzle.triSize/2;
	
	
	public static JScrollBar vertscroll, horizscroll;
	
	public PuzzleGen(String caption)
	{
		super(caption);
	}
	
	public static HashSet<Puzzle> puzzle_set=new HashSet(); 
	
	//complete a puzzle
	public static HashSet<Puzzle> complete_puzzle(Puzzle p)
	{
		HashSet<Puzzle> ret=new HashSet();
		
	
		for(int row=0;row<p.nrows;row++)
		{
		for(int i=0;i<2*row+2;i++)
		{
		if(!p.piece_complete(row,i))
		{
			Vector<String[]> pieces=p.complete_piece(row,i);
				
					
			for(int j=0;j<pieces.size();j++)
			{
				Puzzle newp=p.clone();
				
				newp.set_triangle(row,i,pieces.get(j));
				
				ret.addAll(complete_puzzle(newp));
			}
			return ret;
		}
		
		}
		}
		
		
		if(p.valid())
			ret.add(p);
			
		return ret;
	}
	
	//set limits for the scrollbars
	public static void setScrollMax()
	{
		int addone=0;
		
		if((puzzle_set.size()%numperrow)!=0)
			addone=1;
	
		vertscroll.setMaximum(Math.max(10,((puzzle_set.size()/numperrow)+addone)*(Puzzle.triSize)*((int)((n+1)))+50-frame.getHeight()));
		horizscroll.setMaximum(Math.max(10,(numperrow*(Puzzle.triSize)*(n+1)+50-frame.getWidth())));
	}
	
	public static void main(String args[]) throws Exception
	{
		n=Integer.parseInt(args[0]);
		
		if(args.length<3+n*3)
		{
			System.out.println("PuzzleGen <number of rows in puzzle> - <left boundary labels> - <right boundary labels> - <bottom boundary labels> [-o <image output file>] [-r <number of puzzles per row in display>]");
			System.out.println("Labels should be separated by spaces");
			
			return;
		}
		
		Puzzle p=new Puzzle(n);
		
		String[] left=new String[n];
		String[] right=new String[n];
		String[] bottom=new String[n];
		
		for(int i=0;i<n;i++)
		{
			left[i]=args[i+2];
			right[i]=args[i+n+3];
			bottom[i]=args[i+2*n+4];
		}	
		
		p.setBoundary(left,right,bottom);
		
		puzzle_set=complete_puzzle(p);
		
		
		
		Iterator it=puzzle_set.iterator();
		
		
			
		while(it.hasNext())
		{
			Puzzle pp=(Puzzle)it.next();
		
			for(int i=0;i<pp.puzzle.length;i++)
			{
				System.out.print(pp.puzzle[i]+"   ");
			}
			System.out.println("\nWeight: "+pp.weight());
		}
		
		System.out.println("Number of puzzles: "+puzzle_set.size());
			
		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("-r")) //number per row
			{
				numperrow=Integer.parseInt(args[i+1]);
			}
		}
		
		
		frame=new PuzzleGen("PuzzleGen");
		
		frame.setSize(WIDTH,HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		int addone=0;
		
		if((puzzle_set.size()%numperrow)!=0)
			addone=1;
		
		vertscroll=new JScrollBar(JScrollBar.VERTICAL,0,10,0,Math.max(((puzzle_set.size()/numperrow)+addone)*(Puzzle.triSize)*((int)((n+1)))+50-HEIGHT,10));
		horizscroll=new JScrollBar(JScrollBar.HORIZONTAL,0,10,0,Math.max(numperrow*(Puzzle.triSize)*(n+1)+50-WIDTH,10));
		
		vertscroll.setUnitIncrement(vertinc);
		horizscroll.setUnitIncrement(horizinc);
		
		vertscroll.setBlockIncrement(vertinc*5);
		horizscroll.setBlockIncrement(horizinc*3);
		
		AdjustmentListener adjustmentListener = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
				PuzzleGen.frame.repaint();
			}
		};
		
		vertscroll.addAdjustmentListener(adjustmentListener);
		horizscroll.addAdjustmentListener(adjustmentListener);
		
		
		frame.add(vertscroll,BorderLayout.EAST);
		frame.add(horizscroll,BorderLayout.SOUTH);
		
		im=new BufferedImage(PuzzleGen.frame.getBounds().width-PuzzleGen.vertscroll.getWidth()-PuzzleGen.frame.getInsets().right,PuzzleGen.frame.getBounds().height-(PuzzleGen.frame.getInsets().bottom)-PuzzleGen.horizscroll.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		frame.setVisible(true);
		
		frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
				PuzzleGen.im=new BufferedImage(PuzzleGen.frame.getBounds().width-PuzzleGen.vertscroll.getWidth()-PuzzleGen.frame.getInsets().right,PuzzleGen.frame.getBounds().height-(PuzzleGen.frame.getInsets().bottom)-PuzzleGen.horizscroll.getHeight(),BufferedImage.TYPE_INT_RGB);
                PuzzleGen.setScrollMax();
            }
		});
		
		for(int i=0;i<args.length;i++)
		{
		
			if(args[i].equals("-o")) //image output
			{
				File saveFile=new File(args[i+1]);
				
				BufferedImage bi=new BufferedImage(numperrow*(Puzzle.triSize)*(n+1)+50,
				((puzzle_set.size()/numperrow)+addone)*(Puzzle.triSize)*((int)((n+1)))+50,
					BufferedImage.TYPE_INT_RGB);
					
				Graphics g2=bi.getGraphics();
				
				g2.setColor(Color.white);
				g2.fillRect(0,0,bi.getWidth(),bi.getHeight());
		
				g2.setColor(Color.black);
				
				drawPuzzles(g2,0,0,0,0,1<<30,1<<30);
				
				ImageIO.write(bi,"PNG",saveFile);
			}
		}	
	}
	
	public static void drawPuzzles(Graphics g,int xoff, int yoff, int xmin, int ymin, int xmax, int ymax)
	{
		Iterator it=puzzle_set.iterator();
		
		int ndone=0;
		
		while(it.hasNext())
		{
		Puzzle pp=(Puzzle)it.next();
		
		int puzzlex=(ndone%numperrow)*(n+1)*Puzzle.triSize+50+xoff;
		int puzzley=(ndone/numperrow)*Puzzle.triSize*(n+1)+50+yoff;
		
		if(puzzlex>=xmin&&puzzlex<=xmax&&puzzley>=ymin&&puzzley<=ymax)
		{
			Color oldcolor=g.getColor();
			pp.draw(g,puzzlex,puzzley);
			
			g.setColor(Color.red);
			
			pp.draw_paths(g,puzzlex,puzzley);
			
			g.setColor(Color.blue);
			
			pp.draw_paths_2(g,puzzlex,puzzley);
			
			g.setColor(oldcolor);
		}
		ndone++;
		}
	}
	
	public static BufferedImage im;
	
	public void paint(Graphics g)
	{		
		Graphics g2=im.getGraphics();
		
		g2.setColor(Color.black);
		g2.fillRect(0,0,im.getWidth(),im.getHeight());
		
		g2.setColor(Color.white);
		
		drawPuzzles(g2,-horizscroll.getValue(),-vertscroll.getValue(),-Puzzle.triSize*(n+1),-Puzzle.triSize*(n+1),frame.getWidth(),frame.getHeight());
		
		g.drawImage(im,0,0,null);
		
		horizscroll.repaint();
		vertscroll.repaint();
	}
}