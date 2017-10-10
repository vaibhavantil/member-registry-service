# mavenTemplate
A project template that can be used as a starting point for new services.


## Do this

When starting a new project follow the following list!

1. Decide what to name the new project
2. In pom.xml update __groupId__, __artifactId__,__version__, __name__ and __description__.
3. Update the source files so that they conform to the new package structure.
4. Update the avdl files in the  _resources/avdl_ folder.
4. Rename MustRenameApplication
5. Modify or remove the supplied aggregates, commands, events...
7. Update this README file.
6. Live long and prosper.



- Sign up/Sign in
  - Log each sign in request
  - Create hedvigToken
  - Send customerCreated events
- Retrieie hedvigId for a personalInformationNumber
- Retrieve customerInformation give a ssn
- Retrieve customerInformation from a given ssn
- Retrieve


Entities
 - Customer
   - HedvigId
   - Personal identification number
   - Name
   - Address (Street, Zip, City)
   - Email
   - Phone number
   - Country

   - Auth requests
   -

 - Commands
   - Create customer
     - personalNumber
     - prefferedName
     - fullName