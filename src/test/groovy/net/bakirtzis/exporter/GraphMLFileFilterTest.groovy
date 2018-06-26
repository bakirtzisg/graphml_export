package net.bakirtzis.exporter

import spock.lang.Specification

class GraphMLFileFilterTest extends Specification {
    def "GraphML File Filter matches graphml files"(String filename, boolean shouldAccept) {
        given:
        File file = Mock(File) {
            getName() >> filename
            isDirectory() >> false
        }

        and:
        GraphMLFileFilter filter = new GraphMLFileFilter()

        when:
        boolean accepted = filter.accept(file)

        then:
        accepted == shouldAccept

        where:
        filename | shouldAccept
        "foo.graphml" | true
        "bar.graphml" | true
        "something.xml" | false
        "foo.pdf" | false
        "graphml" | false
        ".graphml" | false
    }

    def "GraphMLFileFilter always accepts directories"() {
        given:
        File file = Mock(File) {
            isDirectory() >> true
        }

        and:
        GraphMLFileFilter filter = new GraphMLFileFilter()

        when:
        boolean accepted = filter.accept(file)

        then:
        accepted
    }

    def "GraphMLFileExporter has the correct description"() {
        expect:
        new GraphMLFileFilter().getDescription() == "GraphML Files"
    }
}
