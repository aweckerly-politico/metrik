package metrik.project.domain.service.githubactions

import metrik.exception.ApplicationException
import metrik.project.builds
import metrik.project.credential
import metrik.project.domain.model.Pipeline
import metrik.project.domain.repository.BuildRepository
import metrik.project.pipelineID
import metrik.project.url
import metrik.project.userInputURL
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate

@ExtendWith(SpringExtension::class)
@Import(GithubActionsPipelineService::class, RestTemplate::class)
@RestClientTest
internal class GithubActionsPipelineServiceTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var githubActionsPipelineService: GithubActionsPipelineService

    @MockBean
    private lateinit var buildRepository: BuildRepository

    private lateinit var mockServer: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    @Test
    fun `should throw exception when verify pipeline given response is 500`() {
        mockServer.expect(MockRestRequestMatchers.requestTo("$url/actions/runs?per_page=1"))
            .andRespond(
                MockRestResponseCreators.withServerError()
            )

        Assertions.assertThrows(ApplicationException::class.java) {
            githubActionsPipelineService.verifyPipelineConfiguration(
                Pipeline(credential = credential, url = userInputURL)
            )
        }
    }

    @Test
    fun `should throw exception when verify pipeline given response is 400`() {

        mockServer.expect(MockRestRequestMatchers.requestTo("$url/actions/runs?per_page=1"))
            .andRespond(
                MockRestResponseCreators.withBadRequest()
            )

        Assertions.assertThrows(ApplicationException::class.java) {
            githubActionsPipelineService.verifyPipelineConfiguration(
                Pipeline(credential = credential, url = userInputURL)
            )
        }
    }

    @Test
    fun `should return sorted stage name lists when getStagesSortedByName() called`() {
        `when`(buildRepository.getAllBuilds(pipelineID)).thenReturn(builds)

        val result = githubActionsPipelineService.getStagesSortedByName(pipelineID)

        Assertions.assertEquals(5, result.size)
        Assertions.assertEquals("amazing", result[0])
        Assertions.assertEquals("build", result[1])
        Assertions.assertEquals("clone", result[2])
        Assertions.assertEquals("good", result[3])
        Assertions.assertEquals("zzz", result[4])
    }

//    @Test
//    fun `should sync builds given build has no stages`() {
//        val mockServer = MockRestServiceServer.createServer(restTemplate)
//
//        `when`(buildRepository.getBuildNumbersNeedSync(projectId, 1)).thenReturn(listOf(1))
//        mockServer.expect(MockRestRequestMatchers.requestTo(getBuildSummariesUrl))
//            .andExpect { MockRestRequestMatchers.header("Authorization", credential) }
//            .andRespond(
//                MockRestResponseCreators.withSuccess(
//                    this.javaClass.getResource("/pipeline/bamboo/raw-build-summary-2.json").readText(),
//                    MediaType.APPLICATION_JSON
//                )
//            )
//
//        mockServer.expect(MockRestRequestMatchers.requestTo(getBuildDetailsUrl))
//            .andExpect { MockRestRequestMatchers.header("Authorization", credential) }
//            .andRespond(
//                MockRestResponseCreators.withSuccess(
//                    this.javaClass.getResource("/pipeline/bamboo/raw-build-details-2.json").readText(),
//                    MediaType.APPLICATION_JSON
//                )
//            )
//
//        bambooPipelineService.syncBuildsProgressively(pipeline, mockEmitCb)
//
//        val build = Build(
//            pipelineId = pipelineId, number = 1, result = Status.SUCCESS, duration = 0, timestamp = 1593398522798,
//            url = "$baseUrl/rest/api/latest/result/$planKey-1",
//            stages = emptyList(),
//            changeSets = emptyList()
//        )
//
//        Mockito.verify(buildRepository, Mockito.times(1)).save(build)
//    }
}
