import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
          useMaterial3: true
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  Future<void> StartServiceinAndroid()async{
    if(Platform.isAndroid) {
      var methodChannel = MethodChannel("messages");
      String data = await methodChannel.invokeMethod("StartService");
      debugPrint(data);
    }
  }
  Future<void> StopServiceinAndroid()async{
    if(Platform.isAndroid) {
      var methodChannel = MethodChannel("messages");
      String data = await methodChannel.invokeMethod("StopService");
      debugPrint(data);}}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              child: Text("Start Service"),
              onPressed: (){StartServiceinAndroid();},
            ),
            ElevatedButton(
              child: Text("Stop Service"),
              onPressed: (){StopServiceinAndroid();},
            )
          ],
        ),
      ),

    );
  }
}