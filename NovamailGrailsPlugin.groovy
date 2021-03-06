class NovamailGrailsPlugin {
    // the plugin version
    def version = "0.3.6"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Novamail Plugin" // Headline display name of the plugin
    def author = "Omasirichukwu Udeinya"
    def authorEmail = ""
    def description = '''\
The Novamail plug-in provides e-mail sending and retrieving capabilities to a \n\
Grails application. It is also capable of sending emails asynchronously by using a scheduled Job.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/novamail"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: 'Novadge', url: 'http://www.novadge.com/']

    // Any additional developers beyond the author specified above.
    def developers = [
        [name: 'Omasirichukwu Udeinya', email: 'omasiri@novadge.com']
    ]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/Omasiri/novamail/issues']

    // Online location of the plugin's browseable source code.
    def scm = [url: 'https://github.com/Omasiri/novamail']

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
