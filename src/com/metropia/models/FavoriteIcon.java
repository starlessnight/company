package com.metropia.models;

import android.content.Context;

public enum FavoriteIcon {
	
	home, work, office, star, schedule, friend, restaurant, fastfood, pencil, coffee, airport,  fruit, 
	gift, place, pharmacy, guitar, music, repair_shop, football, magnifier, sunglasses, zoo, temperature, credit_card;
	
	public static final Integer FIRST_PAGE = Integer.valueOf(1);
	public static final Integer SECOND_PAGE = Integer.valueOf(2);
	
    public static FavoriteIcon fromName(String name, FavoriteIcon failback) {
    	for(FavoriteIcon type : values()) {
    		if(type.name().equals(name)) {
    			return type;
    		}
    	}
    	return failback;
   	}
    	
   	public static Integer getIconResourceId(Context ctx, String name) {
   		return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
  	}
   	
   	public Integer getResourceId(Context ctx) {
   		return getIconResourceId(ctx, name());
   	}
   	
   	public Integer getShadowResourceId(Context ctx) {
   		return getIconResourceId(ctx, name() + "_with_shadow");
   	}
   	
   	public Integer getFavoritePageResourceId(Context ctx) {
   		return getIconResourceId(ctx, "favorite_page_" + name());
   	}
   	
   	public static FavoriteIcon[][] getIcons(Integer pageNo) {
   		switch(pageNo) {
   		  case 1:
   			  return getFirstPageIcons();
   		  case 2:
   			  return getSecondPageIcons();
   		  default:
   			  return getFirstPageIcons();
   		}
   	}
   	
   	public static FavoriteIcon[][] getFirstPageIcons() {
   		return new FavoriteIcon[][] { {home, work, office, star}, {schedule, friend, restaurant, fastfood}, {pencil, coffee, airport, fruit} };
   	}
   	
   	public static FavoriteIcon[][] getSecondPageIcons() {
   		return new FavoriteIcon[][] { {gift, place, pharmacy, guitar}, {music, repair_shop, football, magnifier}, {sunglasses, zoo, temperature, credit_card} };
   	}

}