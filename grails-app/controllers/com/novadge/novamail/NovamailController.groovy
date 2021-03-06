package com.novadge.novamail
import com.novadge.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.mail.*
import javax.mail.Message.RecipientType
import javax.mail.search.FlagTerm
import javax.mail.search.ReceivedDateTerm
import javax.mail.search.*
import static org.springframework.http.HttpStatus.*
import grails.converters.JSON
import grails.transaction.Transactional
import com.novadge.vaultcore.*
@Transactional(readOnly = true)
class NovamailController {

def messagingService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def index(){
        redirect action:'inbox'
    }
    def inbox(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        long unreadCount = MessageIn.countByStatus("Unread")
        
        respond MessageIn.list(params), model:[messageInCount: MessageIn.count(),unreadCount:unreadCount]
    }
    
    //String incomingMailServer, String store, String username, String password
    @Transactional
    def refresh(){

       String mailProvider = ""
       String mailUsername = ""
       String mailPassword = ""
       def date = new Date() - 1

       def term = new ReceivedDateTerm(ComparisonTerm.GE,date) // yesterday and today
       def store = grailsApplication.config.novamail.store
       def hostProps = grailsApplication.config.novamail.hostProps
       def m = messagingService?.getMessages(term)
       MessageIn msg = null
       m.each({
          log.debug it
          msg = new MessageIn()
          messagingService.saveMessage(it,msg)     
       })
   
  
       redirect(action:'index')
    }
    
    @Transactional
    def showIn(MessageIn messageIn){
        
        String body = messagingService.getMessageBody(messageIn,"TEXT/HTML")
        long unreadCount = MessageIn.countByStatus("Unread")
        messageIn.status = "Read"
        messageIn.save(flush:true)
        respond messageIn, model:[unreadCount:unreadCount,body:body]
    }
    
    @Transactional
    def showOut(MessageOut messageOut){
        long unreadCount = MessageIn.countByStatus("Unread")
        String body = messagingService.getMessageBody(messageOut)
        respond messageOut, model:[unreadCount:unreadCount,body:body]
    }
    
    @Transactional
    def delete(MessageIn messageInInstance) {
        
        if (messageInInstance == null) {
            notFound()
            return
        }
        
        messageInInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'messageIn.label', default: 'Message'), messageInInstance.id])
                redirect action:'inbox'
            }
            '*'{ render status: NO_CONTENT }
        }
    }
    
    def compose(){
       respond new MessageOut(params),view:'compose'
    }
    
    @Transactional
    def saveMsgOut(MessageOut messageOut){
        
       
        String mailProvider = "Gmail"
        String mailUsername = ""
        String mailPassword = ""
        def email = "omasiri@hotmail.com"
        Map hostProps = grailsApplication.config.novamail.hostProps
        def props = [hostname:mailProvider,senders:mailUsername,username:mailUsername,password:mailPassword,hostProperties:hostProps]
        messageOut.properties = props
        if (messageOut.hasErrors()) {
            
            respond messageOut.errors, view:'compose',model:[entity:"provider"]
            return
        }
        //print messageOut
        List<File> attachments = []
        
        
        if(params?.attachment?.getOriginalFilename()){
            //print "fake attachement"
            def f = request.getFile("attachment")
            def file = new File(f.getOriginalFilename())
            FileOutputStream fout = new FileOutputStream(file)
            fout.write(f.getBytes())
            attachments.add(file)
        }
        
        
       try{
           print "trying to send msgs"
           messagingService.sendEmail(email,params?.subject, params.body?.toString())
       }
       catch(Exception ex){
           print "${ex.toString()}"
           flash.message = ex.toString()
           respond messageOut, view:'compose'
           return
       }
        
        request.withFormat {
            form {
                flash.message =  "message sent"//message(code: 'default.created.message', args: [message(code: 'race.label', default: 'Race'), raceInstance.id])
                redirect action:'outbox'
            }
            //'*' { respond raceInstance, [status: CREATED] }
        }
    }
    
        
    def outbox(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        long unreadCount = MessageIn.countByStatus("Unread")
        respond MessageOut.list(params), model:[messageOutCount: MessageOut.count(),unreadCount:unreadCount]
    }
    
    def ajaxListInbox(){
        
        
        def query = {
            or{
               rlike("senders", "${params.term}") 
               rlike("subject", "${params.term}")
               rlike("status", "${params.term}")
            }
            
            
            
                          
        projections { // good to select only the required columns.
                property("id")
                property("senders")
                property("subject")
            }
        }
       
        def msgList = MessageIn.createCriteria().list(query)
        
        def map = [:]
       // print rxnconsoList
        def ajaxList = []
        msgList.each({
                map = [:]       
                map.put('id',it[0])
                map.put('label',"${it[1]} :: ${it[2]}")
                map.put('value',"${it[1]} :: ${it[2]}")

                
                ajaxList.add(map)
        })
    
    render ajaxList as JSON
    }
    
    def ajaxListOutbox(){
        
        
        def query = {
            or{
               rlike("recipients", "${params.term}") 
               rlike("subject", "${params.term}")
               rlike("status", "${params.term}")
            }
            
            
            
                          
        projections { // good to select only the required columns.
                property("id")
                property("recipients")
                property("subject")
            }
        }
       
        def msgList = MessageOut.createCriteria().list(query)
        
        def map = [:]
       // print rxnconsoList
        def ajaxList = []
        msgList.each({
                map = [:]       
                map.put('id',it[0])
                map.put('label',"${it[1]} :: ${it[2]}")
                map.put('value',"${it[1]} :: ${it[2]}")
//                  map.id = it.id
//                  map.label = it.LONG_COMMON_NAME
//                  map.value = it.LONG_COMMON_NAME
                
                ajaxList.add(map)
        })
    
    render ajaxList as JSON
    }
    
    def display(MessageIn messageInInstance){ 
       // print "inside display"
       // def messageInInstance = MessageIn.get(params.id)
         if (messageInInstance == null) {
            notFound()
            return
         }
            String content = messagingService.getMessageBody(messageInInstance)
            
            OutputStream out = response.getOutputStream()
            
            out.write(content.getBytes())
            out.close()
       
    }
    
    def displayOut(MessageOut messageOutInstance){ 
        //print "inside display"
       // def messageInInstance = MessageIn.get(params.id)
         if (messageOutInstance == null) {
            notFound()
            return
         }
            String content = messagingService.getMessageBody(messageOutInstance)
           //response.setContentType( "text/html") 
            OutputStream out = response.getOutputStream()
            out.write(content.getBytes())
            out.close()
       
    }
    
    
    
    def download(Attachment attachmentInstance){
       // print "inside download"
        if (attachmentInstance == null) {
            notFound()
            return
        }
        
        response.setContentType( "application-xdownload")//response.setContentType("${documentInstance?.fileType}")
        response.setHeader("Content-Disposition", "attachment;filename=${attachmentInstance.name}")//response.setHeader("Content-disposition", "filename=${documentInstance.originalFilename}")
        OutputStream out = response.getOutputStream()
        out.write(attachmentInstance.data)
        out.close()

    }
    
    protected void notFound() {
            request.withFormat {
                form multipartForm {
                    flash.message = "Not found"//message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])
                    redirect action: "index", method: "GET"
                }
                '*'{ render status: NOT_FOUND }
            }
        }
}
