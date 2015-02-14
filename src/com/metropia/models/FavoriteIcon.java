package com.metropia.models;

import android.content.Context;

public enum FavoriteIcon {
	
	home, work, office, star, schedule, friend, restaurant, fastfood, pencil, coffee, airport,  fruit, 
	gift, place, pharmacy, guitar, music, repair_shop, football, magnifier, sunglasses, zoo, temperature, credit_card, 
	parking, park, beach, theater, grocery, winery, daycare, atm, hotel, pizzeria, ice_cream, bar, gas_station, bank, 
	school, hospital, museum, government_office, amusement_park, ferry_terminal, stadium, golf_course, shopping_center;
	
	public static final Integer FIRST_PAGE = Integer.valueOf(1);
	public static final Integer SECOND_PAGE = Integer.valueOf(2);
	public static final Integer THIRD_PAGE = Integer.valueOf(3);
	
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
   		  case 3:
   			  return getThirdPageIcons();
   		  default:
   			  return getFirstPageIcons();
   		}
   	}
   	
   	public static FavoriteIcon[][] getFirstPageIcons() {
   		return new FavoriteIcon[][] { {home, work, place, star}, {parking, park, beach, repair_shop}, {coffee, theater, airport, grocery} };
   	}
   	
   	public static FavoriteIcon[][] getSecondPageIcons() {
   		return new FavoriteIcon[][] { {winery, daycare, pharmacy, atm}, {hotel, fastfood, pizzeria, ice_cream}, {football, bar, zoo, guitar} };
   	}
   	
   	public static FavoriteIcon[][] getThirdPageIcons() {
   		return new FavoriteIcon[][] { {gas_station, bank, school, hospital}, {museum, restaurant, government_office, amusement_park}, {ferry_terminal, stadium, golf_course, shopping_center} };
   	}

}