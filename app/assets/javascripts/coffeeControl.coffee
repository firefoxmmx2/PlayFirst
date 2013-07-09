$ ->
	#coffee map
	array = [1,2,3,4,5]
	result = (item.name for item in array)
	console.log('(item.name for item in array) = '+result)
	
	#coffee filter
	result=(item for item in array when item > 2)
	console.log('(item for in array when item > 2) = '+result)
	#coffee indexOf in
	result = "s" in "string"
	console.log('"s" in "string" = '+result)
	result = !!~ 'string'.indexOf('s')
	console.log("!!~ 'string' indexOf 's' = "+result)
