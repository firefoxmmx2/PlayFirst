moduleKeywords = ['extended','included']

class Module
  @extend:(obj) ->
    for key,value of obj when key not in moduleKeywords
      @[key] = value
    obj.extended?.apply(@)
    this  
  @include:(obj) ->
    for key, value of obj when key not in moduleKeywords
      @::[key] = value
    obj.included?.apply(@)
    this
   
classProperties=
  find:(id) -> console.log('this is find method')
  create: (attrs) -> 
    console.log('this is attrs method')

instanceProperties= 
  save: () -> console.log('this is save method')

class User extends Module
  @extend classProperties
  @include instanceProperties
  
$ ->
  user = User.find(1)
  User.create()
  user = new User
  user.save()
 