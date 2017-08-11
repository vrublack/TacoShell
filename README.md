# Introduction

This command-line tool written in Java lets you track what you eat during the day. As there are many commercial website that do exactly that, here are some advantages of TacoShell:

* It is extremely quick to use. Instead of clicking through menus, you can just type `add 340g chicken breast`.

* It works offline which is good from a privacy and reliability standpoint. (To have your data on multiple devices, you can put it in your Dropbox, Onedrive etc. folder so it'll automatically sync.)

* You can modify and extend it all you want. For example, I added code to scrape the website of my university dining hall.


# Setup & Getting Started

I will say that there is a bit of a learning curve to using this app, but be assured that it is super simple once you remember a few commands. Here are a few things to get started:

* The app runs in the console. To make things easier, you can use a `.bat` file under Windows (there's a sample file included in the release) or a `.command` file under Mac OS to change into the directory and call `java -jar TacoShell.jar`
* There are several simple commands to add and remove items or to switch to a different day.
  * `add [quantity] [food]`, eg. add `200g chicken breast`). This searches in the current database (USDA by default for [food]). You can use various units like <i>g, kg, oz, ml, cups</i>... and for some foods also just a number (<i>2 tomatoes</i>). It is even possible to use kcal as your quantity in which case that many calories worth of the food is added.
  * `a [quantity] [food]` does the same but automatically chooses the best match without prompting you.
  * You can also use either `add` or `a` to enter micronutrients, macronutrients or calories directly, such as `a 20mg Vitamin C`, or `a 250 kcal`. You can add multiple ones at once, like `a 20g carbs, 30g fat, 15g protein`.
  * It is possible to use a multiplier in the end of the add command, like `add 30g carbs * 2.5`.
* Use the `report` command to see what you've eaten during the day. You can configure the nutrients to show and their respective units in the file `preferences.ini`. In this file, you can also choose to have the report shown after every command that changes it.

You can type `help` for a list of all commands and a detailed explanation of them.

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

I use GWT to compile many classes from this project to javascript for my website [tacoshell.io](http://nutrition-tracker.appspot.com), so it's no surprise that both behave fairly similarly. 

I have decided not to release the website's source for now.
