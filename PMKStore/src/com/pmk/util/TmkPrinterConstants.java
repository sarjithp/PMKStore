package com.pmk.util;

public class TmkPrinterConstants extends PrinterConstants
{
	
	public TmkPrinterConstants() {
		super();
		super.PAPER_CUT=PAPER_CUT;
		super.PAGE_MODE=PAGE_MODE;
		super.STANDARD_MODE=STANDARD_MODE;
		super.LEFT_ALIGN=LEFT_ALIGN;
		super.RIGHT_ALIGN=RIGHT_ALIGN;
		super.MULTIPLE_LINE_FEED=MULTIPLE_LINE_FEED;
		super.BIG_FONT=BIG_FONT;
		super.FONT_NORMAL=FONT_NORMAL;
		super.FONT_NORMAL_BOLD=FONT_NORMAL_BOLD;
		super.FONT_DOUBLE_HEIGHT=FONT_DOUBLE_HEIGHT;
		super.FONT_DOUBLE_WIDTH=FONT_DOUBLE_WIDTH;
		super.FONT_DOUBLE=FONT_DOUBLE;
		super.BAR_CODE=BAR_CODE;
	}
	public static  String SLIP_PRINTER_THERMAL = "Slip - Thermal";
	public static  String SLIP_PRINTER_9PIN = "Slip - 9 Pin";
	public static  String NORMAL_PRINTER = "Normal";
	public static  String EPSON_COMPATIBLE="Epson Comaptible";
	
	public static  int PRINTER_9PIN_WIDTH = 40; 
	public static  int PRINTER_SLIP_WIDTH = 64; 
	public static  int PRINTER_DEFAULT_WIDTH = PRINTER_9PIN_WIDTH;
	public String BAR_CODE=new String(new byte[]{ GS , 'k', 72 });//CODE93
	
	    public  static String PAPER_CUT = new String(new byte[]{10,29,86,66});
	 // public static final String PAPER_CUT= new String (new byte[]{10,29,56,66});
	    public  static String PAGE_MODE = new String (new byte[]{27,76});
	    public  static String STANDARD_MODE = new String (new byte[]{27,83});  
	    public  static String LEFT_ALIGN=new String (new byte[]{27,97,2});  
	    public  static String RIGHT_ALIGN=new String (new byte[]{27,97,2,80});
	    public  static String MULTIPLE_LINE_FEED= new String (new byte []{27,100,2});//5 lines   
	    public  static String BIG_FONT=new String (new byte[]{27,33,8});
	    public  static String FONT_NORMAL               = ((char)0x1b) + "!" + ((char)0x00);	    
	    public  static String FONT_NORMAL_BOLD    = ((char)0x1b) + "!" + ((char)0x08);
	    public  static String FONT_DOUBLE_HEIGHT = ((char)0x1b) + "!" + ((char)0x10);
	    public  static String FONT_DOUBLE_WIDTH  = ((char)0x1b) + "!" + ((char)0x20);
	    public  static String FONT_DOUBLE               = ((char)0x1b) + "!" + ((char)0x30);
	 
	    /* Ommited bcs common
		 public static final String CENTER_ALIGN=new String (new byte[]{27,97,1});   
		  public static final String LINE_FEED= new String (new byte []{10});
		  public static final String H_TAB = new String (new byte[]{9});
		  public static final String SOUND_BUZZER=new String (new byte[]{27,30});
		  public static final String SETTING_LEFT_MARGIN=new String (new byte[]{29,76,5,0});  
		  public static final String SMALL_FONT=new String (new byte[]{27,33,1});
	    public static final String ABS_POS_LOC1=new String (new byte[]{27,36,0,0});
	    public static final String ABS_POS_LOC2=new String (new byte[]{27,36,100,1});
	    public static final String ABS_POS_LOC3=new String (new byte[]{27,36,(byte)145,1});
	    public static final String ABS_POS_LOC4=new String (new byte[]{27,36,(byte)200,1});
	    public static final String ABS_POS_LOC5=new String (new byte[]{27,36,1,1});
	    public static final String CHARACTER_SPACING=new String (new byte[]{27,32,0});
	    
	    //Not found for ESC/POS
	     public static final String FONT_SMALL                   = ((char)0x1b) + "!" + ((char)0x01);
	     public static final String LOGO1= new String(new byte[]{28,112,1,48});
	     public static final String ABS_POSITION=new String (new byte[]{});
		 public static final String H_FULL_LINE_TOP;
		 public static final String H_FULL_LINE_BOTTOM;
	    
	     static
	     {
	    	int length = 60;
	    	byte hFullLineTop[] = new byte[length];
	    	
	    	for(int i = 0; i < length; i++)
	    		hFullLineTop[i] = (byte)223;
	    	
	    	H_FULL_LINE_TOP = new String(hFullLineTop);
	    	
	    	byte hFullLineBottom[] = new byte[length];
	    	
	    	for(int i = 0; i < length; i++)
	    		hFullLineBottom[i] = (byte)220;
	    	
	    	H_FULL_LINE_BOTTOM = new String(hFullLineBottom);
	     }
	    */
}
