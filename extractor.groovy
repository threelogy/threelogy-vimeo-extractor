@Grapes(
    @Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
)

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON

class VimeoExtractor {

	private String token
	private HTTPBuilder http

	VimeoExtractor(String token) {
		this.token = token
		this.http = new HTTPBuilder("https://api.vimeo.com")
		this.http.setHeaders([Authorization: "Bearer ${token}"])
	}

	List<Album> getAlbums() {
		http.request(GET, JSON) { req ->
			uri.path = "/me/albums"
			uri.query = [direction: 'desc', sort: 'date', per_page: 100]
			response.success = {resp, json ->
				return json.data.collect{ album ->
					new Album(uri: album.uri, name: album.name, link: album.link)
				}
			}
		}
	}

	List<Video> getVideos(Album album) {
		http.request(GET, JSON) { req ->
			uri.path = "${album.uri}/videos"
			uri.query = [direction: 'desc', sort: 'date', per_page: 100]
			response.success = {resp, json ->
				return json.data.collect { video ->
					String playerUrl = video.link.replace('https://vimeo.com/', 'https://player.vimeo.com/video/')

					String pictureUri = video.pictures.uri

					Video retVideo = new Video(
							uri: video.uri,
							name: video.name,
							link: video.link,
							playerUrl: playerUrl)

					if (pictureUri) {
						String pictureId = pictureUri.split('/')[-1]
						retVideo.imageLargeUrl = "https://i.vimeocdn.com/video/${pictureId}_1920x700.jpg"
						retVideo.imageSmallUrl = "https://i.vimeocdn.com/video/${pictureId}_750x500.jpg"
					}

					return retVideo
				}
			}
		}
	}

}

class Album {
	String uri
	String name
	String link

	@Override
	String toString() {
		"Album [${name}][${uri}]"
	}
}

class Video{
	String uri
	String name
	String link
	String imageLargeUrl
	String imageSmallUrl
	String playerUrl


	@Override
	public String toString() {
		"Video [${name}][${uri}]"
	}
}

VimeoExtractor x = new VimeoExtractor("6db0c4c5f8d1dd2b520cf0781ae4d1dc")

x.albums.each{ Album album ->
	println "*******"
	println "name: ${album.name}"
	println "link: ${album.link}"
	println "videos: "

	x.getVideos(album).each{ Video video ->
		println "    ------"
		println "    name: ${video.name}"
		println "    link: ${video.link}"
		println "    playerUrl: ${video.playerUrl}"
		println "    imageLargeUrl: ${video.imageLargeUrl}"
		println "    imageSmallUrl: ${video.imageSmallUrl}"
		println "    ------"
	}
}