/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.util;

import java.util.HashMap;
import java.util.Map;

import leitej.exception.ImplementationLtRtException;

/**
 * 
 * @author  Julio Leite
 */
public enum ColorEnum {
	
	BASIC_BLACK, BASIC_WHITE, BASIC_RED, BASIC_LIME, BASIC_BLUE, BASIC_YELLOW, BASIC_CYAN_AQUA, 
	BASIC_MAGENTA_FUCHSIA, BASIC_SILVER, BASIC_GRAY, BASIC_MAROON, BASIC_OLIVE, BASIC_GREEN, 
	BASIC_PURPLE, BASIC_TEAL, BASIC_NAVY,

	MAROON, DARK_RED, BROWN, FIREBRICK, CRIMSON, RED, TOMATO, CORAL,
	INDIAN_RED, LIGHT_CORAL, DARK_SALMON, SALMON, LIGHT_SALMON,
	
	ORANGE_RED,
	DARK_ORANGE, ORANGE, GOLD, DARK_GOLDEN_ROD, GOLDEN_ROD, PALE_GOLDEN_ROD,
	DARK_KHAKI, KHAKI, OLIVE, YELLOW,
	
	YELLOW_GREEN,
	DARK_OLIVE_GREEN, OLIVE_DRAB, LAWN_GREEN, CHART_REUSE, GREEN_YELLOW,
	DARK_GREEN, GREEN, FOREST_GREEN, LIME, LIME_GREEN, LIGHT_GREEN,
	PALE_GREEN, DARK_SEA_GREEN, MEDIUM_SPRING_GREEN, SPRING_GREEN,
	
	SEA_GREEN,
	MEDIUM_AQUA_MARINE, MEDIUM_SEA_GREEN, LIGHT_SEA_GREEN, DARK_SLATE_GRAY, TEAL, 
	DARK_CYAN, AQUA, CYAN, LIGHT_CYAN, DARK_TURQUOISE, TURQUOISE, MEDIUM_TURQUOISE,
	PALE_TURQUOISE, AQUA_MARINE, POWDER_BLUE, CADET_BLUE,
	
	STEEL_BLUE, CORN_FLOWER_BLUE, DEEP_SKY_BLUE, DODGER_BLUE, LIGHT_BLUE, SKY_BLUE, LIGHT_SKY_BLUE,
	MIDNIGHT_BLUE, NAVY, DARK_BLUE, MEDIUM_BLUE, BLUE, ROYAL_BLUE,
	
	BLUE_VIOLET, INDIGO, DARK_SLATE_BLUE, SLATE_BLUE, MEDIUM_SLATE_BLUE, MEDIUM_PURPLE,
	DARK_MAGENTA, DARK_VIOLET, DARK_ORCHID, MEDIUM_ORCHID, PURPLE, THISTLE,
	PLUM, VIOLET, MAGENTA_FUCHSIA, ORCHID, MEDIUM_VIOLET_RED, PALE_VIOLET_RED,
	
	DEEP_PINK, HOT_PINK, LIGHT_PINK, PINK, ANTIQUE_WHITE, BEIGE, BISQUE, BLANCHED_ALMOND,
	WHEAT, CORN_SILK, LEMON_CHIFFON, LIGHT_GOLDEN_ROD_YELLOW, LIGHT_YELLOW,
	
	SADDLE_BROWN, SIENNA, CHOCOLATE, PERU, SANDY_BROWN, BURLY_WOOD, TAN,
	ROSY_BROWN, MOCCASIN, NAVAJO_WHITE, PEACH_PUFF, MISTY_ROSE, LAVENDER_BLUSH,
	LINEN, OLD_LACE, PAPAYA_WHIP, SEA_SHELL, MINT_CREAM,
	
	SLATE_GRAY, LIGHT_SLATE_GRAY, LIGHT_STEEL_BLUE, LAVENDER, FLORAL_WHITE, 
	ALICE_BLUE, GHOST_WHITE, HONEYDEW, IVORY, AZURE, SNOW,
	
	BLACK, DIM_GRAY, GRAY, DARK_GRAY, SILVER, LIGHT_GRAY,
	GAINSBORO, WHITE_SMOKE, WHITE;
	
	
	private static final Map<ColorEnum, Integer> COLOR_MAP;
	static{
		COLOR_MAP = new HashMap<ColorEnum, Integer>();
		
		COLOR_MAP.put(BASIC_BLACK,				Integer.valueOf(ColorUtil.rgbColor(   0,   0,   0)));
		COLOR_MAP.put(BASIC_WHITE,				Integer.valueOf(ColorUtil.rgbColor( 255, 255, 255)));
		COLOR_MAP.put(BASIC_RED,				Integer.valueOf(ColorUtil.rgbColor( 255,   0,   0)));
		COLOR_MAP.put(BASIC_LIME,				Integer.valueOf(ColorUtil.rgbColor(   0, 255,   0)));
		COLOR_MAP.put(BASIC_BLUE,				Integer.valueOf(ColorUtil.rgbColor(   0,   0, 255)));
		COLOR_MAP.put(BASIC_YELLOW,				Integer.valueOf(ColorUtil.rgbColor( 255, 255,   0)));
		COLOR_MAP.put(BASIC_CYAN_AQUA,			Integer.valueOf(ColorUtil.rgbColor(   0, 255, 255)));
		COLOR_MAP.put(BASIC_MAGENTA_FUCHSIA,	Integer.valueOf(ColorUtil.rgbColor( 255,   0, 255)));
		COLOR_MAP.put(BASIC_SILVER,				Integer.valueOf(ColorUtil.rgbColor( 192, 192, 192)));
		COLOR_MAP.put(BASIC_GRAY,				Integer.valueOf(ColorUtil.rgbColor( 128, 128, 128)));
		COLOR_MAP.put(BASIC_MAROON,				Integer.valueOf(ColorUtil.rgbColor( 128,   0,   0)));
		COLOR_MAP.put(BASIC_OLIVE,				Integer.valueOf(ColorUtil.rgbColor( 128, 128,   0)));
		COLOR_MAP.put(BASIC_GREEN,				Integer.valueOf(ColorUtil.rgbColor(   0, 128,   0)));
		COLOR_MAP.put(BASIC_PURPLE,				Integer.valueOf(ColorUtil.rgbColor( 128,   0, 128)));
		COLOR_MAP.put(BASIC_TEAL,				Integer.valueOf(ColorUtil.rgbColor(   0, 128, 128)));
		COLOR_MAP.put(BASIC_NAVY,				Integer.valueOf(ColorUtil.rgbColor(   0,   0, 128)));

		COLOR_MAP.put(MAROON,					Integer.valueOf(ColorUtil.rgbColor( 128,   0,   0)));
		COLOR_MAP.put(DARK_RED,					Integer.valueOf(ColorUtil.rgbColor( 139,   0,   0)));
		COLOR_MAP.put(BROWN,					Integer.valueOf(ColorUtil.rgbColor( 165,  42,  42)));
		COLOR_MAP.put(FIREBRICK,				Integer.valueOf(ColorUtil.rgbColor( 178,  34,  34)));
		COLOR_MAP.put(CRIMSON,					Integer.valueOf(ColorUtil.rgbColor( 220,  20,  60)));
		COLOR_MAP.put(RED,						Integer.valueOf(ColorUtil.rgbColor( 255,   0,   0)));
		COLOR_MAP.put(TOMATO,					Integer.valueOf(ColorUtil.rgbColor( 255,  99,  71)));
		COLOR_MAP.put(CORAL,					Integer.valueOf(ColorUtil.rgbColor( 255, 127,  80)));
		COLOR_MAP.put(INDIAN_RED,				Integer.valueOf(ColorUtil.rgbColor( 205,  92,  92)));
		COLOR_MAP.put(LIGHT_CORAL,				Integer.valueOf(ColorUtil.rgbColor( 240, 128, 128)));
		COLOR_MAP.put(DARK_SALMON,				Integer.valueOf(ColorUtil.rgbColor( 233, 150, 122)));
		COLOR_MAP.put(SALMON,					Integer.valueOf(ColorUtil.rgbColor( 250, 128, 114)));
		COLOR_MAP.put(LIGHT_SALMON,				Integer.valueOf(ColorUtil.rgbColor( 255, 160, 122)));
		
		COLOR_MAP.put(ORANGE_RED,				Integer.valueOf(ColorUtil.rgbColor( 255,  69,   0)));
		COLOR_MAP.put(DARK_ORANGE,				Integer.valueOf(ColorUtil.rgbColor( 255, 140,   0)));
		COLOR_MAP.put(ORANGE,					Integer.valueOf(ColorUtil.rgbColor( 255, 165,   0)));
		COLOR_MAP.put(GOLD,						Integer.valueOf(ColorUtil.rgbColor( 255, 215,   0)));
		COLOR_MAP.put(DARK_GOLDEN_ROD,			Integer.valueOf(ColorUtil.rgbColor( 184, 134,  11)));
		COLOR_MAP.put(GOLDEN_ROD,				Integer.valueOf(ColorUtil.rgbColor( 218, 165,  32)));
		COLOR_MAP.put(PALE_GOLDEN_ROD,			Integer.valueOf(ColorUtil.rgbColor( 238, 232, 170)));
		COLOR_MAP.put(DARK_KHAKI,				Integer.valueOf(ColorUtil.rgbColor( 189, 183, 107)));
		COLOR_MAP.put(KHAKI,					Integer.valueOf(ColorUtil.rgbColor( 240, 230, 140)));
		COLOR_MAP.put(OLIVE,					Integer.valueOf(ColorUtil.rgbColor( 128, 128,   0)));
		COLOR_MAP.put(YELLOW,					Integer.valueOf(ColorUtil.rgbColor( 255, 255,   0)));
		
		COLOR_MAP.put(YELLOW_GREEN,				Integer.valueOf(ColorUtil.rgbColor( 154, 205,  50)));
		COLOR_MAP.put(DARK_OLIVE_GREEN,			Integer.valueOf(ColorUtil.rgbColor(  85, 107,  47)));
		COLOR_MAP.put(OLIVE_DRAB,				Integer.valueOf(ColorUtil.rgbColor( 107, 142,  35)));
		COLOR_MAP.put(LAWN_GREEN,				Integer.valueOf(ColorUtil.rgbColor( 124, 252,   0)));
		COLOR_MAP.put(CHART_REUSE,				Integer.valueOf(ColorUtil.rgbColor( 127, 255,   0)));
		COLOR_MAP.put(GREEN_YELLOW,				Integer.valueOf(ColorUtil.rgbColor( 173, 255,  47)));
		COLOR_MAP.put(DARK_GREEN,				Integer.valueOf(ColorUtil.rgbColor(   0, 100,   0)));
		COLOR_MAP.put(GREEN,					Integer.valueOf(ColorUtil.rgbColor(   0, 128,   0)));
		COLOR_MAP.put(FOREST_GREEN,				Integer.valueOf(ColorUtil.rgbColor(  34, 139,  34)));
		COLOR_MAP.put(LIME,						Integer.valueOf(ColorUtil.rgbColor(   0, 255,   0)));
		COLOR_MAP.put(LIME_GREEN,				Integer.valueOf(ColorUtil.rgbColor(  50, 205,  50)));
		COLOR_MAP.put(LIGHT_GREEN,				Integer.valueOf(ColorUtil.rgbColor( 144, 238, 144)));
		COLOR_MAP.put(PALE_GREEN,				Integer.valueOf(ColorUtil.rgbColor( 152, 251, 152)));
		COLOR_MAP.put(DARK_SEA_GREEN,			Integer.valueOf(ColorUtil.rgbColor( 143, 188, 143)));
		COLOR_MAP.put(MEDIUM_SPRING_GREEN,		Integer.valueOf(ColorUtil.rgbColor(   0, 250, 154)));
		COLOR_MAP.put(SPRING_GREEN,				Integer.valueOf(ColorUtil.rgbColor(   0, 255, 127)));
		
		COLOR_MAP.put(SEA_GREEN,				Integer.valueOf(ColorUtil.rgbColor(  46, 139,  87)));
		COLOR_MAP.put(MEDIUM_AQUA_MARINE,		Integer.valueOf(ColorUtil.rgbColor( 102, 205, 170)));
		COLOR_MAP.put(MEDIUM_SEA_GREEN,			Integer.valueOf(ColorUtil.rgbColor(  60, 179, 113)));
		COLOR_MAP.put(LIGHT_SEA_GREEN,			Integer.valueOf(ColorUtil.rgbColor(  32, 178, 170)));
		COLOR_MAP.put(DARK_SLATE_GRAY,			Integer.valueOf(ColorUtil.rgbColor(  47,  79,  79)));
		COLOR_MAP.put(TEAL,						Integer.valueOf(ColorUtil.rgbColor(   0, 128, 128)));
		COLOR_MAP.put(DARK_CYAN,				Integer.valueOf(ColorUtil.rgbColor(   0, 139, 139)));
		COLOR_MAP.put(AQUA,						Integer.valueOf(ColorUtil.rgbColor(   0, 255, 255)));
		COLOR_MAP.put(CYAN,						Integer.valueOf(ColorUtil.rgbColor(   0, 255, 255)));
		COLOR_MAP.put(LIGHT_CYAN,				Integer.valueOf(ColorUtil.rgbColor( 224, 255, 255)));
		COLOR_MAP.put(DARK_TURQUOISE,			Integer.valueOf(ColorUtil.rgbColor(   0, 206, 209)));
		COLOR_MAP.put(TURQUOISE,				Integer.valueOf(ColorUtil.rgbColor(  64, 224, 208)));
		COLOR_MAP.put(MEDIUM_TURQUOISE,			Integer.valueOf(ColorUtil.rgbColor(  72, 209, 204)));
		COLOR_MAP.put(PALE_TURQUOISE,			Integer.valueOf(ColorUtil.rgbColor( 175, 238, 238)));
		COLOR_MAP.put(AQUA_MARINE,				Integer.valueOf(ColorUtil.rgbColor( 127, 255, 212)));
		COLOR_MAP.put(POWDER_BLUE,				Integer.valueOf(ColorUtil.rgbColor( 176, 224, 230)));
		COLOR_MAP.put(CADET_BLUE,				Integer.valueOf(ColorUtil.rgbColor(  95, 158, 160)));
		
		COLOR_MAP.put(STEEL_BLUE,				Integer.valueOf(ColorUtil.rgbColor(  70, 130, 180)));
		COLOR_MAP.put(CORN_FLOWER_BLUE,			Integer.valueOf(ColorUtil.rgbColor( 100, 149, 237)));
		COLOR_MAP.put(DEEP_SKY_BLUE,			Integer.valueOf(ColorUtil.rgbColor(   0, 191, 255)));
		COLOR_MAP.put(DODGER_BLUE,				Integer.valueOf(ColorUtil.rgbColor(  30, 144, 255)));
		COLOR_MAP.put(LIGHT_BLUE,				Integer.valueOf(ColorUtil.rgbColor( 173, 216, 230)));
		COLOR_MAP.put(SKY_BLUE,					Integer.valueOf(ColorUtil.rgbColor( 135, 206, 235)));
		COLOR_MAP.put(LIGHT_SKY_BLUE,			Integer.valueOf(ColorUtil.rgbColor( 135, 206, 250)));
		COLOR_MAP.put(MIDNIGHT_BLUE,			Integer.valueOf(ColorUtil.rgbColor(  25,  25, 112)));
		COLOR_MAP.put(NAVY,						Integer.valueOf(ColorUtil.rgbColor(   0,   0, 128)));
		COLOR_MAP.put(DARK_BLUE,				Integer.valueOf(ColorUtil.rgbColor(   0,   0, 139)));
		COLOR_MAP.put(MEDIUM_BLUE,				Integer.valueOf(ColorUtil.rgbColor(   0,   0, 205)));
		COLOR_MAP.put(BLUE,						Integer.valueOf(ColorUtil.rgbColor(   0,   0, 255)));
		COLOR_MAP.put(ROYAL_BLUE,				Integer.valueOf(ColorUtil.rgbColor(  65, 105, 225)));
		
		COLOR_MAP.put(BLUE_VIOLET,				Integer.valueOf(ColorUtil.rgbColor( 138,  43, 226)));
		COLOR_MAP.put(INDIGO,					Integer.valueOf(ColorUtil.rgbColor(  75,   0, 130)));
		COLOR_MAP.put(DARK_SLATE_BLUE,			Integer.valueOf(ColorUtil.rgbColor(  72,  61, 139)));
		COLOR_MAP.put(SLATE_BLUE,				Integer.valueOf(ColorUtil.rgbColor( 106,  90, 205)));
		COLOR_MAP.put(MEDIUM_SLATE_BLUE,		Integer.valueOf(ColorUtil.rgbColor( 123, 104, 238)));
		COLOR_MAP.put(MEDIUM_PURPLE,			Integer.valueOf(ColorUtil.rgbColor( 147, 112, 219)));
		COLOR_MAP.put(DARK_MAGENTA,				Integer.valueOf(ColorUtil.rgbColor( 139,   0, 139)));
		COLOR_MAP.put(DARK_VIOLET,				Integer.valueOf(ColorUtil.rgbColor( 148,   0, 211)));
		COLOR_MAP.put(DARK_ORCHID,				Integer.valueOf(ColorUtil.rgbColor( 153,  50, 204)));
		COLOR_MAP.put(MEDIUM_ORCHID,			Integer.valueOf(ColorUtil.rgbColor( 186,  85, 211)));
		COLOR_MAP.put(PURPLE,					Integer.valueOf(ColorUtil.rgbColor( 128,   0, 128)));
		COLOR_MAP.put(THISTLE,					Integer.valueOf(ColorUtil.rgbColor( 216, 191, 216)));
		COLOR_MAP.put(PLUM,						Integer.valueOf(ColorUtil.rgbColor( 221, 160, 221)));
		COLOR_MAP.put(VIOLET,					Integer.valueOf(ColorUtil.rgbColor( 238, 130, 238)));
		COLOR_MAP.put(MAGENTA_FUCHSIA,			Integer.valueOf(ColorUtil.rgbColor( 255,   0, 255)));
		COLOR_MAP.put(ORCHID,					Integer.valueOf(ColorUtil.rgbColor( 218, 112, 214)));
		COLOR_MAP.put(MEDIUM_VIOLET_RED,		Integer.valueOf(ColorUtil.rgbColor( 199,  21, 133)));
		COLOR_MAP.put(PALE_VIOLET_RED,			Integer.valueOf(ColorUtil.rgbColor( 219, 112, 147)));
		
		COLOR_MAP.put(DEEP_PINK,				Integer.valueOf(ColorUtil.rgbColor( 255,  20, 147)));
		COLOR_MAP.put(HOT_PINK,					Integer.valueOf(ColorUtil.rgbColor( 255, 105, 180)));
		COLOR_MAP.put(LIGHT_PINK,				Integer.valueOf(ColorUtil.rgbColor( 255, 182, 193)));
		COLOR_MAP.put(PINK,						Integer.valueOf(ColorUtil.rgbColor( 255, 192, 203)));
		COLOR_MAP.put(ANTIQUE_WHITE,			Integer.valueOf(ColorUtil.rgbColor( 250, 235, 215)));
		COLOR_MAP.put(BEIGE,					Integer.valueOf(ColorUtil.rgbColor( 245, 245, 220)));
		COLOR_MAP.put(BISQUE,					Integer.valueOf(ColorUtil.rgbColor( 255, 228, 196)));
		COLOR_MAP.put(BLANCHED_ALMOND,			Integer.valueOf(ColorUtil.rgbColor( 255, 235, 205)));
		COLOR_MAP.put(WHEAT,					Integer.valueOf(ColorUtil.rgbColor( 245, 222, 179)));
		COLOR_MAP.put(CORN_SILK,				Integer.valueOf(ColorUtil.rgbColor( 255, 248, 220)));
		COLOR_MAP.put(LEMON_CHIFFON,			Integer.valueOf(ColorUtil.rgbColor( 255, 250, 205)));
		COLOR_MAP.put(LIGHT_GOLDEN_ROD_YELLOW,	Integer.valueOf(ColorUtil.rgbColor( 250, 250, 210)));
		COLOR_MAP.put(LIGHT_YELLOW,				Integer.valueOf(ColorUtil.rgbColor( 255, 255, 224)));
		
		COLOR_MAP.put(SADDLE_BROWN,				Integer.valueOf(ColorUtil.rgbColor( 139,  69,  19)));
		COLOR_MAP.put(SIENNA,					Integer.valueOf(ColorUtil.rgbColor( 160,  82,  45)));
		COLOR_MAP.put(CHOCOLATE,				Integer.valueOf(ColorUtil.rgbColor( 210, 105,  30)));
		COLOR_MAP.put(PERU,						Integer.valueOf(ColorUtil.rgbColor( 205, 133,  63)));
		COLOR_MAP.put(SANDY_BROWN,				Integer.valueOf(ColorUtil.rgbColor( 244, 164,  96)));
		COLOR_MAP.put(BURLY_WOOD,				Integer.valueOf(ColorUtil.rgbColor( 222, 184, 135)));
		COLOR_MAP.put(TAN,						Integer.valueOf(ColorUtil.rgbColor( 210, 180, 140)));
		COLOR_MAP.put(ROSY_BROWN,				Integer.valueOf(ColorUtil.rgbColor( 188, 143, 143)));
		COLOR_MAP.put(MOCCASIN,					Integer.valueOf(ColorUtil.rgbColor( 255, 228, 181)));
		COLOR_MAP.put(NAVAJO_WHITE,				Integer.valueOf(ColorUtil.rgbColor( 255, 222, 173)));
		COLOR_MAP.put(PEACH_PUFF,				Integer.valueOf(ColorUtil.rgbColor( 255, 218, 185)));
		COLOR_MAP.put(MISTY_ROSE,				Integer.valueOf(ColorUtil.rgbColor( 255, 228, 225)));
		COLOR_MAP.put(LAVENDER_BLUSH,			Integer.valueOf(ColorUtil.rgbColor( 255, 240, 245)));
		COLOR_MAP.put(LINEN,					Integer.valueOf(ColorUtil.rgbColor( 250, 240, 230)));
		COLOR_MAP.put(OLD_LACE,					Integer.valueOf(ColorUtil.rgbColor( 253, 245, 230)));
		COLOR_MAP.put(PAPAYA_WHIP,				Integer.valueOf(ColorUtil.rgbColor( 255, 239, 213)));
		COLOR_MAP.put(SEA_SHELL,				Integer.valueOf(ColorUtil.rgbColor( 255, 245, 238)));
		COLOR_MAP.put(MINT_CREAM,				Integer.valueOf(ColorUtil.rgbColor( 245, 255, 250)));
		
		COLOR_MAP.put(SLATE_GRAY,				Integer.valueOf(ColorUtil.rgbColor( 112, 128, 144)));
		COLOR_MAP.put(LIGHT_SLATE_GRAY,			Integer.valueOf(ColorUtil.rgbColor( 119, 136, 153)));
		COLOR_MAP.put(LIGHT_STEEL_BLUE,			Integer.valueOf(ColorUtil.rgbColor( 176, 196, 222)));
		COLOR_MAP.put(LAVENDER,					Integer.valueOf(ColorUtil.rgbColor( 230, 230, 250)));
		COLOR_MAP.put(FLORAL_WHITE,				Integer.valueOf(ColorUtil.rgbColor( 255, 250, 240)));
		COLOR_MAP.put(ALICE_BLUE,				Integer.valueOf(ColorUtil.rgbColor( 240, 248, 255)));
		COLOR_MAP.put(GHOST_WHITE,				Integer.valueOf(ColorUtil.rgbColor( 248, 248, 255)));
		COLOR_MAP.put(HONEYDEW,					Integer.valueOf(ColorUtil.rgbColor( 240, 255, 240)));
		COLOR_MAP.put(IVORY,					Integer.valueOf(ColorUtil.rgbColor( 255, 255, 240)));
		COLOR_MAP.put(AZURE,					Integer.valueOf(ColorUtil.rgbColor( 240, 255, 255)));
		COLOR_MAP.put(SNOW,						Integer.valueOf(ColorUtil.rgbColor( 255, 250, 250)));
		
		COLOR_MAP.put(BLACK,					Integer.valueOf(ColorUtil.rgbColor(   0,   0,   0)));
		COLOR_MAP.put(DIM_GRAY,					Integer.valueOf(ColorUtil.rgbColor( 105, 105, 105)));
		COLOR_MAP.put(GRAY,						Integer.valueOf(ColorUtil.rgbColor( 128, 128, 128)));
		COLOR_MAP.put(DARK_GRAY,				Integer.valueOf(ColorUtil.rgbColor( 169, 169, 169)));
		COLOR_MAP.put(SILVER,					Integer.valueOf(ColorUtil.rgbColor( 192, 192, 192)));
		COLOR_MAP.put(LIGHT_GRAY,				Integer.valueOf(ColorUtil.rgbColor( 211, 211, 211)));
		COLOR_MAP.put(GAINSBORO,				Integer.valueOf(ColorUtil.rgbColor( 220, 220, 220)));
		COLOR_MAP.put(WHITE_SMOKE,				Integer.valueOf(ColorUtil.rgbColor( 245, 245, 245)));
		COLOR_MAP.put(WHITE,					Integer.valueOf(ColorUtil.rgbColor( 255, 255, 255)));
	}
	
	public int rbgColor(){
		Integer rbgColor = COLOR_MAP.get(this);
		if(rbgColor == null) throw new ImplementationLtRtException();
		return rbgColor;
	}
	
	public String webColor(){
		Integer rbgColor = COLOR_MAP.get(this);
		if(rbgColor == null) throw new ImplementationLtRtException();
		return ColorUtil.webColor(rbgColor);
	}
	
	public int redComponent(){
		Integer rbgColor = COLOR_MAP.get(this);
		if(rbgColor == null) throw new ImplementationLtRtException();
		return ColorUtil.redComponent(rbgColor);
	}
	
	public int greenComponent(){
		Integer rbgColor = COLOR_MAP.get(this);
		if(rbgColor == null) throw new ImplementationLtRtException();
		return ColorUtil.greenComponent(rbgColor);
	}
	
	public int blueComponent(){
		Integer rbgColor = COLOR_MAP.get(this);
		if(rbgColor == null) throw new ImplementationLtRtException();
		return ColorUtil.blueComponent(rbgColor);
	}
	
}
