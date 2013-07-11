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
	#coffee property each
	object={a:'a',b:'b',c:'c',d:'d'}
	for key, value in object
		console.log "#{key} == #{value}"
	#coffee max min
	console.log('Math.max array = '+Math.max(array) )
	console.log('Math.min array = '+Math.min(array) )
	#coffee true/false
	str = "migrating coconuts"
	console.log('string == string => ' + str == str)
	console.log('string is string => '+ str is str)
	hash = undefined
	hash or={}
	console.log('hash or= {}; hash => ' + hash)
	hash ?={}
	console.log('hash ?= {}; hash => ' + hash)
	#coffee 析构赋值
	someobject = {a:'value for a', b:'value for b'}
	{a,b}= someobject
	console.log "a is '#{a}', b is '#{b}'"
	{join,resolve} = reqire("path")
	join('/Users','Alex')
	#coffee 私有变量
	#Execute function immediately
	type = do ->
		classToType = {}
		for name in "Boolean Number String Function Array Date RegExp Underfined Null".split(" ")
			classToType["[object "+ name +"]"] = name.toLowerCase()
	#Return a function
		(obj) ->
			strType = Object::toString.call(obj)
			classToType[strType] or "object"
	
