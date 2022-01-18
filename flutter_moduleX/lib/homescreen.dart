import 'package:flutter/material.dart';



class HomeScreen extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    
       var brightness = MediaQuery.of(context).platformBrightness;
    bool darkModeOn = brightness == Brightness.dark;

    Color dayNightTextColor = Colors.black;
    if (darkModeOn) {
      dayNightTextColor = Colors.white;
    } else {
      dayNightTextColor = Colors.black;
    }

    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the PrivacySecurityPage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(
          "Flutter Module",
          style: TextStyle(color: dayNightTextColor),
        ),
        elevation: 0.0,
        backgroundColor: Colors.transparent,
        automaticallyImplyLeading: false,
      ),
      body: ListView(
        children: <Widget>[
          Padding(
              padding: const EdgeInsets.all(15.0),
              child: Column(
                children: <Widget>[
                 
                  Text(
                    'Flutter Module',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 25),
                  ),
                           
                ],
              ))
        ],
      ),
    );
  
  }
}




