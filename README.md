# Introduction

This project features the core functionality of my website [tacoshell.io](http://nutrition-tracker.appspot.com) in a standalone, text-based Java application.

It helps you <b>track your calories and nutrients many times faster</b> than most calorie tracking websites allow.

You can add a specific food item, like potatos, or add nutrients directly.

A few <b>sample inputs</b>:

* add 340g chicken breast <br/>
* add 1.5 cup of milk <br/>
* add 2 tbsp of olive oil <br/>
* add tomato <i>(which is interpreted as 1 tomato)</i> <br/>
* add 350 calories <br/>
* add 45g carbs, 20g protein <br/>


You can type <b>"help"</b> for a list of all commands.

# Data Sources

Currently, you can choose between the USDA Database and FatSecret API.

* The [USDA Database (also called NDB)](http://ndb.nal.usda.gov/)  is a free and comprehensive food database containing more than 8,000 basic food 
items and also branded food items. The version used in NutritionTracker is derived from Release 26, Abbreviated Version, 
but also contains a metric indicating how common this food is which I calculated using data from What We Eat In America (a health study)
in order to improve search results. 

* The FatSecret API lets you have access to the database of [FatSecret.com](https://www.fatsecret.com). It is much larger than the USDA db, and 
the description of the entries tend to be cleaner and less verbose. However, I have found that some important entries are part of USDA but not of FatSecret, e.g. 3.7% milk.
Unfortunately, the country-specific FatSecret-databases are not supported.

* You can also define your own food items which are stored locally.


# tacoshell.io

I use GWT to compile many classes from this project to javascript for my website [tacoshell.io](http://nutrition-tracker.appspot.com), so it's no surprise that both behave fairly similarly. 
However, I have decided not to release the website's source for now.