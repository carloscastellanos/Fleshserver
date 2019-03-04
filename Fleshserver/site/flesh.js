// holds an instance of XMLHttpRequest
var xmlHttp = createXmlHttpRequestObject();
// holds the remote server address and parameters
var serverAddress = "/setio.spi?io3=0";
// result returned by server
var result = null;
// displays error messages if true
var debugMode = true;


// creates an XMLHttpRequest instance
function createXmlHttpRequestObject() 
{
  // will store the reference to the XMLHttpRequest object
  var xmlHttp;
  // this should work for all browsers except IE
  try
  {
    // try to create XMLHttpRequest object
    xmlHttp = new XMLHttpRequest();
  }
  catch(e)
  {
    // assume IE7 or older
    var XmlHttpVersions = new Array("MSXML2.XMLHTTP.7.0",
    								"MSXML2.XMLHTTP.6.0",
                                    "MSXML2.XMLHTTP.5.0",
                                    "MSXML2.XMLHTTP.4.0",
                                    "MSXML2.XMLHTTP.3.0",
                                    "MSXML2.XMLHTTP",
                                    "Microsoft.XMLHTTP");
    // try every prog id until one works
    for (var i=0; i<XmlHttpVersions.length && !xmlHttp; i++) 
    {
      try 
      { 
        // try to create XMLHttpRequest object
        xmlHttp = new ActiveXObject(XmlHttpVersions[i]);
      } 
      catch (e) {}
    }
  }
  // return the created object or display an error message
  if (!xmlHttp)
    alert("Error creating the XMLHttpRequest object.");
  else 
    return xmlHttp;
}

/*
// function that displays a new message on the page
function display(message)
{
  // obtain a reference to the <div> element on the page
  myDiv = document.getElementById("coll_display");
  
  // display message
  myDiv.innerHTML = message;
}

// function that displays an error message
function displayError(message)
{
  // display error message, with more technical details if debugMode is true
  display("Error retrieving the Collection! " +
          (debugMode ? message : ""));
}
*/

// call server asynchronously
function getFlesh()
{	
  // only continue if xmlHttp isn't void
  if (xmlHttp)
  {
    // try to connect to the server
    try
    {
     	 // make asynchronous HTTP request to retrieve new message
		xmlHttp.open("GET", serverAddress, true);
		xmlHttp.onreadystatechange = handleGettingFlesh;
		xmlHttp.send(null);
    }
    catch(e)
    {
		displayError(e.toString());
    }
  }
}


// function called when the state of the HTTP request changes
function handleGettingFlesh() 
{
  // when readyState is 4, we are ready to read the server response
  if (xmlHttp.readyState == 4) 
  {
    // continue only if HTTP status is "OK"
    if (xmlHttp.status == 200) 
    {
      try
      {
        // do something with the response from the server
       result = handleResponse();
      }
      catch(e)
      {
        // display error message
        displayError(e.toString());
        result = null;
      }
    } 
    else
    {
      // display error message
      displayError(xmlHttp.statusText);   
    }
  }
}

// handles the response received from the server
function handleResponse()
{
  // retrieve the server's response 
  var response = xmlHttp.responseText;
  // server error?
  /*if (response.indexOf("ERRNO") >= 0 
      || response.indexOf("error") >= 0
      || response.length == 0)
    throw(response.length == 0 ? "Server error." : "Server error: " + response);
  */
  if(!response || response.length == 0)
	throw(response.length == 0 ? "Server error." : "Server error: " + response);
  
  
  return response;
}

// function that displays an error message
function displayError(message)
{
  // display error message, with more technical details if debugMode is true
  display("Error retrieving the data! " +
          (debugMode ? message : ""));
}

function display(message)
{
  // obtain a reference to the <div> element on the page
  myDiv = document.getElementById("flesh");
  
  // display message
  myDiv.innerHTML = message;
}


