package com.pmk.util;

public class PrinterConstants {

	public static final byte LF =10 ; //  LINE FEED
	public static final byte HT =9  ;// HORIZONTAL TAB
	public static final byte ESC=27 ;// ESCAPE CHARACTER
	public static final byte FS =28 ;// FILE SEPERATOR
	public static final byte GS =29 ;// GROUP SEPERATOR
	public static final byte FF =12 ;// FORM FEED
	public static final byte RS =30 ;// RECORD SEPERATOR 
	public static final byte ENQ= 5 ;// ENQUIRY
	
	public static  String SLIP_PRINTER_THERMAL = "Slip - Thermal";
	public static  String SLIP_PRINTER_9PIN = "Slip - 9 Pin";
	public static String NORMAL_PRINTER = "Normal";
	public static  String EPSON_COMPATIBLE="Epson Comaptible";
	
	public   int PRINTER_9PIN_WIDTH = 40; 
	public   int PRINTER_SLIP_WIDTH = 64; 
	public   int PRINTER_DEFAULT_WIDTH = PRINTER_9PIN_WIDTH;
	
	//common for both
	public static final  String LINE_FEED			=new String(new byte[]{ LF});                
	public static final  String H_TAB				=new String(new byte[]{ HT});
	public static final  String SOUND_BUZZER 		=new String(new byte[]{ ESC, RS}); //??
	public static final  String CENTER_ALIGN			=new String(new byte[]{ ESC, 'a', 1});   
	public static final  String SETTING_LEFT_MARGIN	=new String(new byte[]{ GS,'L',5,0});
	public static final  String SMALL_FONT			=new String(new byte[]{ ESC, '!', 1});
	public static final  String ABS_POS_LOC1			=new String(new byte[]{ ESC, '$', 0, 0});    
	public static final  String ABS_POS_LOC2			=new String(new byte[]{ ESC, '$', 100, 1});  
	public static final  String ABS_POS_LOC3			=new String(new byte[]{ ESC, '$', (byte)145, 1});  
	public static final  String ABS_POS_LOC4			=new String(new byte[]{ ESC, '$', (byte)200, 1});  
	public static final  String ABS_POS_LOC5			=new String(new byte[]{ ESC, '$', 1, 1}); 
	public static final  String CHARACTER_SPACING	=new String(new byte[]{ ESC, ' ', 0});
	public  static final String LOGO1_NEW= new String(new byte[]{GS, '(', 'L', 6, 0, 48, 69, 70, 70, 1, 1});// prints the graphic located in Key 70 ,70
	public  static final String LOGO21_NEW= new String(new byte[]{GS, '(', 'L', 6, 0, 48, 69, 71, 71, 1, 1});// prints the graphic located in Key 71 ,71
	public  static final String LOGO1= new String(new byte[]{FS,'p',1,48});
	public  static final String LOGO2= new String(new byte[]{FS,'p',2,48});
	
	//not found for ESC/POS
	public  static final String ABS_POSITION=new String (new byte[]{});
    public  static final String FONT_SMALL                   = ((char)0x1b) + "!" + ((char)0x01);
    

	   
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
	    
	    public   String PAPER_CUT=null			;
		public   String PAGE_MODE=null			;
		public   String STANDARD_MODE=null		;    
		public   String LEFT_ALIGN=null 			;
		public   String RIGHT_ALIGN=null			;       
		public   String MULTIPLE_LINE_FEED=null	;      
		public   String BIG_FONT=null			;            
		public   String FONT_NORMAL=null			;     
		public   String FONT_NORMAL_BOLD=null	;       
		public   String FONT_DOUBLE_HEIGHT=null	;      
		public   String FONT_DOUBLE_WIDTH =null  ;         
		public   String FONT_DOUBLE	=null		;  
		public   String BAR_CODE =null;
		public String BAR_CODE_HRI_POS = new String(new byte[]{ GS , 'H', 2 });
		public String BAR_CODE_HEIGHT_100 = new String(new byte[]{ GS , 'h', 100 });
		public String BAR_CODE_WIDTH_2 = new String(new byte[]{ GS , 'w', 2 });
}

