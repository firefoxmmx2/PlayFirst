$ ->
  $("h1").text("hahaha , this is a trap hahaha")
  class Animal
    constructor:(name) -> 
      @name = name
      console.log("Animal name is #{name}")
  class Person extends Animal
    constructor:(@name) ->
      # @name = "Person"
      super(@name)
      console.log("Person name is #{name}")

   a = new Animal("pig")
   b = new Person("Andy")
