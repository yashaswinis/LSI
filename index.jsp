<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>


<%@ page import="com.amazonaws.*"%>
<%@ page import="com.amazonaws.auth.*"%>
<%@ page import="com.amazonaws.services.ec2.*"%>
<%@ page import="com.amazonaws.services.ec2.model.*"%>
<%@ page import="com.amazonaws.services.s3.*"%>
<%@ page import="com.amazonaws.services.s3.model.*"%>
<%@ page import="com.amazonaws.services.dynamodbv2.*"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.*"%>

<%! // Share the client objects across threads to
    // avoid creating new clients for each web request
    private AmazonEC2         ec2;
    private AmazonS3           s3;
    private AmazonDynamoDB dynamo;
 %>

<%
    /*
     * AWS Elastic Beanstalk checks your application's health by periodically
     * sending an HTTP HEAD request to a resource in your application. By
     * default, this is the root or default resource in your application,
     * but can be configured for each environment.
     *
     * Here, we report success as long as the app server is up, but skip
     * generating the whole page since this is a HEAD request only. You
     * can employ more sophisticated health checks in your application.
     */
    if (request.getMethod().equals("HEAD")) return;
%>

<%
    if (ec2 == null) {
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        ec2    = new AmazonEC2Client(credentialsProvider);
        s3     = new AmazonS3Client(credentialsProvider);
        dynamo = new AmazonDynamoDBClient(credentialsProvider);
    }
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Hello AWS Web World!</title>
<link rel="stylesheet" href="styles/styles.css" type="text/css"
	media="screen">
</head>
<body>
	<div id="content" class="container">
		<div class="section grid grid5 s3">
			<h2>Amazon S3 Buckets:</h2>
			<ul>
				<% for (Bucket bucket : s3.listBuckets()) { %>
				<li><%= bucket.getName() %></li>
				<% } %>
			</ul>
		</div>

		<div class="section grid grid5 sdb">
			<h2>Amazon DynamoDB Tables:</h2>
			<ul>
				<% for (String tableName : dynamo.listTables().getTableNames()) { %>
				<li><%= tableName %></li>
				<% } %>
			</ul>
		</div>

		<div class="section grid grid5 gridlast ec2">
			<h2>Amazon EC2 Instances:</h2>
			<ul>
				<% for (Reservation reservation : ec2.describeInstances().getReservations()) { %>
				<% for (Instance instance : reservation.getInstances()) { %>
				<li><%= instance.getInstanceId() %></li>
				<% } %>
				<% } %>
			</ul>
		</div>
	</div>
	<p>SID(SverID of the server executing the client request): ${SID}</p>
	<p>Session Data:</p>
	<p>Message: ${message}</p>
	<p>Version: ${version }</p>
	<p>Session Expiration time: ${expire}</p>
	<p>Session Discard Time: ${discard}</p>
	<p>SverIDprimary and SverIDBackup :${locationmetadata}</p>
	<p>Response of session data is from :${svrresponse}</p>
	<p>Current Server's view:${serverview}</p>



	<FORM NAME="form1" METHOD=POST>
		<INPUT TYPE="HIDDEN" NAME="buttonName"> <INPUT TYPE="BUTTON"
			VALUE="Replace" ONCLICK="button1()"> <INPUT TYPE=TEXT
			NAME=replacetext SIZE=50> <br /> <INPUT TYPE="BUTTON"
			VALUE="Refresh" ONCLICK="button2()"> <br /> <INPUT
			TYPE="BUTTON" VALUE="Logout" ONCLICK="button3()"> <br />
	</FORM>

	<SCRIPT LANGUAGE="JavaScript">
 <!--
                   function button1()     
                   {
                       document.form1.buttonName.value = "button 1";
                       form1.submit();
                   }    
                   function button2()
                   {
                       document.form1.buttonName.value = "button 2";
                       form1.submit();
                   }
                   function button3()
                   {
                       document.form1.buttonName.value = "button 3";
                       form1.submit();
                   }      
                      
                 -->
            </SCRIPT>
</body>
</body>
</html>