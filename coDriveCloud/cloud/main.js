// STATUS: REQUEST_SENT, REQUEST_ACCEPTED, REQUEST_COMPLETED, REQUEST_REJECTED
// TYPE: STORE, RETRIEVE

//Background Job running (Remove Expired Data):


Parse.Cloud.define("AddStoreRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  // Data has been uploaded
  // Add an entry in interaction for the data
  // Add an entry in log for the data
  // Input Expected: ownerId, keeperId, dataObjectId
  // Output Expected: Success or Failure
  // Add new Interaction
  // Add new Log Entry
  // Add new ServerStatus Entry
  // Push: Keeper: "Hey! your friend wishes to use some of your space!"
  
  
  var Owner = Parse.Object.extend("User");
  var owner = new Owner();
  owner.id = request.params.ownerId;
  
  var Keeper = Parse.Object.extend("User");
  var keeper = new Keeper();
  keeper.id = request.params.keeperId;
  
  var Data = Parse.Object.extend("Data");
  var data = new Data();  
  data.id = request.params.dataObjectId;
  
  var Log = Parse.Object.extend("Log");
  var log = new Log();
  log.set('status','REQUEST_SENT');
  log.save(null,{
	  success:function(log){
		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.set("to", keeper);
		  interaction.set("from", owner);
		  interaction.set("data", data);
		  interaction.set("type", 'STORE');
		  interaction.set("status", 'REQUEST_SENT');
		  var relation = interaction.relation("log");
		  relation.add(log);
		  
		  interaction.save(null, {
			success: function(result) {
   		      var ServerStatus = Parse.Object.extend("ServerStatus");
			  var serverStatus = new ServerStatus();  
			  serverStatus.set("keeper", keeper);
			  serverStatus.set("owner", owner);
			  serverStatus.set("data", data);
			  serverStatus.set("interaction",interaction);
			  serverStatus.save(null,{
				  success:function(serverStatus){
					  var query = new Parse.Query(Parse.Installation);
						query.equalTo('user',serverStatus.get("keeper"));
						
						Parse.Push.send({
						  where: query, // Set our Installation query
						  data: {
							alert: "Hey! your friend wishes to use some of your space!"
						  }
						});
					  response.success("Request Sent!");
				  },
				  error:function(error){
					  response.error(error.message);
				  }
			  });
			},
			error: function(interaction, error) {
			  response.error(error.message);
			}
		  });
	  },
	  error:function(log,error){
		  response.error(error.message);
	  }
  });  
});


Parse.Cloud.define("RespondToStoreRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  var user = request.user;
  // Data has been uploaded
  // Notification for Accepting Data
  // Add an entry in log for the data
  // Input Expected: isAccepted, serverStatusId
  // Output Expected: Success or Failure
  // Change Interaction Status
  // Maybe Remove ServerStatus Entry
  // Maybe Remove Data from table
  // Push: Owner: "Oops! your friend isn't willing to help you out at the moment!"
  // Keeper: "Great! The file is ready to be downloaded!"
  
  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id = request.params.serverStatusId;
  
  serverStatus.fetch({
	  success:function(serverStatus){
		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
		  
		  interaction.fetch({
			  success:function(interaction){
				  var isAccepted = request.params.isAccepted;
		  
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  
				  if(isAccepted==true){
					 log.set('status','REQUEST_ACCEPTED');
					 interaction.set('status','REQUEST_ACCEPTED');	
				  }
				  else{
					 log.set('status','REQUEST_REJECTED');
					 interaction.set('status','REQUEST_REJECTED');
				  }
				  log.save(null,{
					 success:function(log){
						  var relation = interaction.relation("log");
						  relation.add(log);
						  
						  interaction.save(null, {
							success: function(interaction) {
								
							  if(isAccepted==true){
									var query = new Parse.Query(Parse.Installation);
									query.equalTo('user',serverStatus.get("keeper"));
									
									Parse.Push.send({
									  where: query, // Set our Installation query
									  data: {
										alert: "Great! The file is ready to be downloaded!"
									  }
									});
								 response.success("Responded to Request Successfully!");	
							  }
							  else{
								  interaction.fetch({
									  success:function(){
										  var Data = Parse.Object.extend("Data");
										  var data = new Data();
										  data.id=interaction.get("data").id;
										  data.fetch({
											  success:function(data){
												  data.set("isValid",false);
												  data.save({
													  success:function(data){
															var query = new Parse.Query(Parse.Installation);
															query.equalTo('user',serverStatus.get("owner"));
															
															Parse.Push.send({
															  where: query, // Set our Installation query
															  data: {
																alert: "Oops! your friend isn't willing to help you out at the moment!"
															  }
															});
														  serverStatus.destroy({
																
																 success:function(serverStatus){																	
																	 response.success("Responded to Request Successfully!");
																 },
																 error:function(serverStatus,error){
																	 response.error(error.message);
																 }
														  });
													  },
													  error:function(data,error){
														  response.error(error.message);
													  }
												  });
											  },
											  error:function(data,error){
												  response.error(error.message);
											  }
										  });
									  },
									  error:function(interaction,error){
										  response.error(error.message);
									  }
								  });
								 
							  }
							  
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  }); 
					 } ,
					 error:function(log,error){
						 response.error(error.message);
					 }
				  });
			  },
			  error:function(interaction,error){
				  response.error(error.message);
			  }
		  });
	  },
	  error:function(){
		  response.error(error.message);
	  }
  });
});


Parse.Cloud.define("CompleteStoreRequest", function(request, response) {
  // Data has been downloaded
  // Input Expected: serverStatusId
  // Output Expected: Success or Failure
  // Change Interaction Status
  // Add to Log 
  // Remove Data from Server
  // Push: Owner "Congrats! Now your friend is sharing some data of yours!"
  // Keeper: "Congrats! You are now storing some of your friends data for him/her!"
  
  Parse.Cloud.useMasterKey();
  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id=request.params.serverStatusId;
  serverStatus.fetch({
	  success:function(serverStatus){
  		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
  		      var Keeper = Parse.Object.extend("User");
			  var keeper = new Keeper();
			  keeper.id = serverStatus.get("keeper").id;
		  
		  interaction.fetch({
			  success:function(result){
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  log.set('status','REQUEST_COMPLETED');
				  log.save(null,{
					  success:function(log){
						  interaction.set('status','REQUEST_COMPLETED');	
						  var relation = interaction.relation("log");
						  relation.add(log);
						  interaction.save(null, {
							success: function(result) {
								var Data = Parse.Object.extend("Data");
								  var data = new Data();
								  data.id=interaction.get("data").id;
								  data.fetch({
									  success:function(data){
										  data.set("isValid",false);
										  data.save({
											  success:function(data){
												    keeper.fetch({
														success:function(keeper){
															keeper.set("space",keeper.get("space")-data.get("size"));
															keeper.save(null,{
																success:function(){
																	var query = new Parse.Query(Parse.Installation);
																	query.equalTo('user',serverStatus.get("owner"));
																	
																	Parse.Push.send({
																	  where: query, // Set our Installation query
																	  data: {
																		alert: "Congrats! Now your friend is sharing some data of yours!"
																	  }
																	});
																	var query2 = new Parse.Query(Parse.Installation);
																	query2.equalTo('user',serverStatus.get("keeper"));
																	
																	Parse.Push.send({
																	  where: query2, // Set our Installation query
																	  data: {
																		alert: "Congrats! You are now storing some of your friends data for him/her!"
																	  }
																	});
																  response.success("Request Completed Successfully!");
																},
																error:function(){
																	response.error(error.message);
																}
															});
														},
														error:function(keeper,error){
															response.error(error.message);
														}
													});
											  },
											  error:function(data,error){
												  response.error(error.message);
											  }
										  });
									  },
									  error:function(data,error){
										  response.error(error.message);
									  }
								  });
							  
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  });
					  },
					  error:function(log,error){
						  response.error(error.message);
					  }
				  });
			  },
			  error: function(interaction,error){
				  response.error(error.message);
			  }
		  }); 
	  },
	  error:function(serverStatus,error){
		  response.error(error.message);
	  }
  });
});

Parse.Cloud.define("AddRetrieveRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  // Data has been uploaded
  // Change Interaction Type
  // Change Interaction Status
  // Add an entry in log for the data
  // Input Expected: serverStatusId
  // Output Expected: Success or Failure
  // Push: keeper "Hey, your friend wants some of his data back. Should we confirm your availability?"
      
  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id=request.params.serverStatusId;
  serverStatus.fetch({
	  success:function(serverStatus){
  		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
		  interaction.fetch({
			  success:function(interaction){
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  log.set('status','REQUEST_SENT');
				  log.save(null,{
					  success:function(log){
						  interaction.set('status','REQUEST_SENT');
						  interaction.set('type','RETRIEVE');						  
						  var relation = interaction.relation("log");
						  relation.add(log);
						  interaction.save(null, {
							success: function(result) {
								var query2 = new Parse.Query(Parse.Installation);
								query2.equalTo('user',serverStatus.get("keeper"));
								
								Parse.Push.send({
								  where: query2, // Set our Installation query
								  data: {
									alert: "Hey, your friend wants some of his data back. Should we confirm your availability?"
								  }
								});
							  response.success("Request Sent Successfully!");
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  });
					  },
					  error:function(log,error){
						  response.error(error.message);
					  }
				  });
			  },
			  error: function(interaction,error){
				  response.error(error.message);
			  }
		  }); 
	  },
	  error:function(serverStatus,error){
		  response.error(error.message);
	  }
  });
});

Parse.Cloud.define("RespondToRetrieveRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  // Data is with Keeper
  // Add an entry in log for the data
  // Input Expected: isAccepted, serverStatusId
  // Output Expected: Success or Failure
  // Change Interaction Status
  // Push to owner: "Your friend will be uploading the data to our servers soon."
  // Push to keeper: "Let us know when you can upload the data to our servers."
  
  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id = request.params.serverStatusId;
  var isAccepted = request.params.isAccepted;
  serverStatus.fetch({
	  success:function(serverStatus){		  
		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
		  interaction.fetch({
			  success:function(interaction){
				  
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  
				  if(isAccepted==true){
					 log.set('status','REQUEST_ACCEPTED');
					 interaction.set('status','REQUEST_ACCEPTED');	
				  }
				  else if(isAccepted==false){
					 log.set('status','REQUEST_REJECTED');
					 interaction.set('status','REQUEST_REJECTED');
				  }
				  else{
					  log.set('status','ERROR');
					 interaction.set('status','ERROR');
				  }
				  log.save(null,{
					 success:function(log){
						  var relation = interaction.relation("log");
						  relation.add(log);	  
						  interaction.save(null, {
							success: function(interaction) {
								if(isAccepted==true){
									var query = new Parse.Query(Parse.Installation);
									query.equalTo('user',serverStatus.get("owner"));
									
									Parse.Push.send({
									  where: query, // Set our Installation query
									  data: {
										alert: "Your friend will be uploading the data to our servers soon."
									  }
									});
									var query2 = new Parse.Query(Parse.Installation);
									query2.equalTo('user',serverStatus.get("keeper"));
									
									Parse.Push.send({
									  where: query2, // Set our Installation query
									  data: {
										alert: "Let us know when you can upload the data to our servers."
									  }
									});
								}
								else{
									var query = new Parse.Query(Parse.Installation);
									query.equalTo('user',serverStatus.get("owner"));
									
									Parse.Push.send({
									  where: query, // Set our Installation query
									  data: {
										alert: "Your friend cannot upload the data right now!"
									  }
									});
								}
							  response.success("Responded to Request Successfully!");
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  }); 
					 },
					 error:function(log,error){
						 response.error(error.message);
					 }
				  });
			  },
			  error:function(interaction,error){
				  response.error(error.message);
			  }
		  });
	  },
	  error:function(serverStatus,error){
		  response.error(error.message);
	  }
	});
  });

Parse.Cloud.define("PrecompleteRetrieveRequest", function(request, response) {
    Parse.Cloud.useMasterKey();
  // Data is waiting to be downloaded by the owner
  // Input Expected: serverStatusId
  // Output Expected: Success or Failure
  // Add Log entry
  // Change Interaction Status
  // Push send to owner: "Your Data is ready to be received."


  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id=request.params.serverStatusId;
  serverStatus.fetch({
	  success:function(serverStatus){		  
  		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
			  
		  interaction.fetch({
			  success:function(result){
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  log.set('status','ON_SERVER');
				  log.save(null,{
					  success:function(log){
						  interaction.set('status','ON_SERVER');	
						  var relation = interaction.relation("log");
						  relation.add(log);
						  interaction.save(null, {
							success: function(result) {
								var query = new Parse.Query(Parse.Installation);
								query.equalTo('user',serverStatus.get("owner"));
								
								Parse.Push.send({
								  where: query, // Set our Installation query
								  data: {
									alert: "Your Data is ready to be received."
								  }
								});
							  response.success("Request Completed Successfully!");
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  });
					  },
					  error:function(log,error){
						  response.error(error.message);
					  }
				  });
			  },
			  error: function(interaction,error){
				  response.error(error.message);
			  }
		  });	
	  },
	  error:function(serverStatus,error){
		  response.error(error.message);
	  }
  });
});

Parse.Cloud.define("CompleteRetrieveRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  // Data has been downloaded by Owner
  // Input Expected: serverStatusId
  // Output Expected: Success or Failure
  // Delete the Data from the Server
  // Add to Log
  // Remove Entry from ServerStatus
  // Pushes Sent: 1 to owner: "We have deleted your data from our servers. Hope you enjoyed the service"
  
  var ServerStatus = Parse.Object.extend("ServerStatus");
  var serverStatus = new ServerStatus();
  serverStatus.id=request.params.serverStatusId;
  
  serverStatus.fetch({
	  success:function(serverStatus){
			  var Keeper = Parse.Object.extend("User");
			  var keeper = new Keeper();
			  keeper.id = serverStatus.get("keeper").id;
		  		  
		  var Interaction = Parse.Object.extend("Interaction");
		  var interaction = new Interaction();
		  interaction.id = serverStatus.get("interaction").id;
		  interaction.fetch({
			  success:function(result){
				  var Log = Parse.Object.extend("Log");
				  var log = new Log();
				  log.set('status','REQUEST_COMPLETED');
				  log.save(null,{
					  success:function(log){
						  interaction.set('status','REQUEST_COMPLETED');	
						  var relation = interaction.relation("log");
						  relation.add(log);
						  interaction.save(null, {
							success: function(result) {
								  var Data = Parse.Object.extend("Data");
								  var data = new Data();
								  data.id=interaction.get("data").id;
								  data.fetch({
									  success:function(data){
										  data.set("isValid",false);
										  data.save({
											  success:function(data){
												  keeper.fetch({
														success:function(keeper){
															keeper.set("space",keeper.get("space")+data.get("size"));
															keeper.save(null,{
																success:function(){
																	serverStatus.destroy({
																		  success:function(serverStatus){
																				var query = new Parse.Query(Parse.Installation);
																				query.equalTo('user',serverStatus.get("owner"));
																				
																				Parse.Push.send({
																				  where: query, // Set our Installation query
																				  data: {
																					alert: "We have deleted your data from our servers. Hope you enjoyed the service!"
																				  }
																				});
																			  response.success("Request Completed Successfully!");
																		  },
																		  error:function(serverStatus,error){
																			  response.error(error.message);
																		  }
																	  });
																},
																error:function(){
																	response.error(error.message);
																}
															});
														},
														error:function(keeper,error){
															response.error(error.message);
														}
													});
											  },
											  error:function(data,error){
												  response.error(error.message);
											  }
										  });
									  },
									  error:function(data,error){
										  response.error(error.message);
									  }
								  });
							},
							error: function(interaction,error) {
							  response.error(error.message);
							}
						  });
					  },
					  error:function(log,error){
						  response.error(error.message);
					  }
				  });
			  },
			  error: function(interaction,error){
				  response.error(error.message);
			  }
		  });
	  },
	  error:function(serverStatus,error){
		  response.error(error.message);
	  }
  });
});

