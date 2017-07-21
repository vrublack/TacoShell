# Introduction

This project helps you <b>track your calories and nutrients many times faster</b> than most calorie tracking websites allow.

You can add a specific food item, like potatos, or add nutrients directly.

A few <b>sample inputs</b>:

* add 340g chicken breast <br/>
* add 1.5 cup of milk <br/>
* add 2 tbsp of olive oil <br/>
* add tomato <i>(which is interpreted as 1 tomato)</i> <br/>
* add 350 calories <br/>
* add 45g carbs, 20g protein <br/>


# Setup & Getting Started

There might be a bit of a learning curve to this app, but be assured that it is worth it. Here are a few things to get started:

* The app runs in the console. To make things easier, you can use a .bat file under Windows (there's a sample file included in the release) or a .command file under Mac OS to change into the directory and call "java -jar TacoShell.jar"
* There are several simple commands to add and remove items or to switch to a different day.
  * <b>add [quantity] [food]</b>, eg. add <i>200g chicken breast</i>). This searches in the current database (USDA by default for [food]). You can use various units like <i>g, kg, oz, ml, cups</i>... and for some foods also just a number (<i>2 tomatoes</i>). It is even possible to use kcal as your quantity in which case that many calories worth of the food is added.
  * <b>a</b> does the same but automatically chooses the best match without prompting you.
  * You can also use either <i>add</i> or <i>a</i> to enter micronutrients, macronutrients or calories directly, such as <i>a 20mg Vitamin C</i>, or <i>a 250 kcal</i>. You can add multiple ones at once, like <i>a 20g carbs, 30g fat, 15g protein</i>.
  * It is possible to use a multiplier in the end of the add command, like <i>add 30g carbs * 2.5</i>.
* Use the <b>report</b> command to see what you've eaten during the day. You can configure the nutrients to show and their respective units in the file preferences.ini. In this file, you can also choose to have the report shown after every command that changes it.

You can type <b>"help"</b> for a list of all commands and a detailed explanation of them.

# Data Sources

Currently, you can choose between the USDA Database and FatSecret API.

* The [USDA Database (also called NDB)](http://ndb.nal.usda.gov/)  is a free and comprehensive food database containing more than 8,000 basic food 
items and also branded food items. The version used in NutritionTracker is derived from Release 26, Abbreviated Version, 
but also contains a metric indicating how common this food is which I calculated using data from What We Eat In America (a health study)
in order to improve search results. 

* The FatSecret API lets you have access to the database of [FatSecret.com](https://www.fatsecret.com). It is much larger than the USDA db, and 
the description of the entries tend to be cleaner and less verbose. You have to sign up for your own API key, though.
Unfortunately, the country-specific FatSecret-databases are not supported.

* You can also define your own food items which are stored locally.


# tacoshell.io

I use GWT to compile many classes from this project to javascript for my website [tacoshell.io](http://nutrition-tracker.appspot.com), so it's no surprise that both behave fairly similarly. I would recommend against using the website for anything other than trying out a few things since it is still fairly unstable/buggy.

I have decided not to release the website's source for now.
