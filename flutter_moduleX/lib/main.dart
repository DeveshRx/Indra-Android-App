import 'package:flutter/material.dart';
import 'privacysecurity.dart';
import 'homescreen.dart';

void main() {
  runApp(MyApp());
}

/*class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        brightness: Brightness.light,
        primarySwatch: Colors.blue,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        primarySwatch: Colors.blue,
      ),
      // themeMode: ThemeMode.system,
      home: PrivacySecurityPage(title: 'Privacy & Security'),
      debugShowCheckedModeBanner: false,
    );
  }
}*/


class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        brightness: Brightness.light,
        primarySwatch: Colors.blue,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        primarySwatch: Colors.blue,
      ),
    // initialRoute: '/home',
      routes: {
        // When navigating to the "/" route, build the PrivacySecurityScreen widget.
        '/home': (context) => HomeScreen(),
        // When navigating to the "/second" route, build the SecondScreen widget.
        '/prisec': (context) => PrivacySecurityScreen(),
      },
      debugShowCheckedModeBanner: false,
    );
  }
}

/*
class PrivacySecurityPage extends StatefulWidget {
  PrivacySecurityPage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _PrivacySecurityPageState createState() => _PrivacySecurityPageState();
}

class _PrivacySecurityPageState extends State<PrivacySecurityPage> {
 

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
          widget.title,
          style: TextStyle(color: dayNightTextColor),
        ),
        elevation: 0.0,
        backgroundColor: Colors.transparent,
      ),
      body: ListView(
        children: <Widget>[
          Padding(
              padding: const EdgeInsets.all(15.0),
              child: Column(
                children: <Widget>[
                  Text(
                    'Your calls remain private with end-to-end encryption',
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 25),
                  ),
                  Text(
                    'To keep your conversations private, indra uses end-to-end encryption for calls.',
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 20),
                  ),
                  Text(" "),
                  Text(
                      'Only people in the call will know what\'s said or shown',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: 20)),
                  Text(
                      'indra(and indra team) can\'t see, hear or save your call\'s audio and video.',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: 20)),
                  Text(
                      'End-to-end encryption is a standard security method that protects communications data. It\'s built into every indra call, so you don\'t need to turn it on yourself and it can\'t be turned off.',
                      textAlign: TextAlign.left,
                      style: TextStyle(fontSize: 20)),
                ],
              ))
        ],
      ),
    );
  }
}
*/
