package com.rsupport.mobile.agent.repo.agent

import android.os.Build
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.test.filters.LargeTest
import com.nhaarman.mockitokotlin2.timeout
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.status.AgentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@LargeTest
class AgentRepositoryTest {

    private val TEST_GUID = "guid-123"
    private val delayTime = 200L

    @Mock
    lateinit var agentEntityOvserver: Observer<AgentInfo?>

    private fun createAgentInfo(guid: String): AgentInfo {
        return AgentInfo().apply {
            this.guid = guid
            name = "name-123"
            status = AgentStatus.AGENT_STATUS_NOLOGIN.toInt()
            macaddr = "0:0:0:0"
            localip = "1.2.3.4"
            devicetype = "3"
            extend = ComConstant.RVFLAG_KEY_ANDROID.toString()
            osname = "Android" + Build.VERSION.RELEASE
        }
    }

    @Before
    fun setup() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)
        repository.clearAll()
    }

    // 비동기로 guid 를 가져오는지를 확인한다.
    @Test
    fun syncGuidGetTest() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)
        repository.insert(createAgentInfo(TEST_GUID))

        val guid = repository.getAgentInfo().asFlow().map {
            it.guid ?: ""
        }.first()
        MatcherAssert.assertThat("guid 를 가져오지 못해서 실패", guid, Matchers.`is`(TEST_GUID))
    }


    // 조회 (없는 guid 로 조회 한다.)
    @Test
    fun notfoundGuidLoadTest() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)

        val agnetEntity = repository.findByGuid("1234")

        var findGuid: String? = null

        withContext(Dispatchers.Main) {
            agnetEntity.observeForever(agentEntityOvserver)
            agnetEntity.observeForever {
                findGuid = it.guid
            }
        }
        Mockito.verify(agentEntityOvserver, timeout(1000)).onChanged(Mockito.any(AgentInfo::class.java))
        MatcherAssert.assertThat("agentInfo 가 있어서 실패", findGuid, Matchers.nullValue())
    }

    // 삽입 (guid를 설정하고 insert 하고 조회한다.)
    @Test
    fun insertAgentTest() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)
        val agnetEntity = repository.findByGuid(TEST_GUID)
        var findGuid: String? = null
        withContext(Dispatchers.Main) {
            agnetEntity.observeForever(agentEntityOvserver)
            agnetEntity.observeForever {
                findGuid = it.guid
            }
        }

        repository.insert(createAgentInfo(TEST_GUID))
        Mockito.verify(agentEntityOvserver, timeout(1000).times(2)).onChanged(Mockito.any(AgentInfo::class.java))
        MatcherAssert.assertThat("agentInfo guid 가 달라서 실패", findGuid, Matchers.`is`(TEST_GUID))
    }

    // 삽입 > 삭제 (guid를 설정하고 insert 하고 delete 한다.)
    @Test
    fun insertDeleteAgentTest() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)
        var findGuid: String? = null

        val agnetEntity = repository.findByGuid(TEST_GUID)
        withContext(Dispatchers.Main) {
            agnetEntity.observeForever(agentEntityOvserver)
            agnetEntity.observeForever {
                findGuid = it.guid
            }
        }

        repository.insert(createAgentInfo(TEST_GUID))
        delay(delayTime)
        MatcherAssert.assertThat("guid 가 달라서 실패", findGuid, Matchers.`is`(TEST_GUID))


        repository.delete(TEST_GUID)
        delay(delayTime)
        MatcherAssert.assertThat("guid 가 삭제되지 않아서 실패", findGuid, Matchers.nullValue())
    }


    // 삽입 > 삭제 (guid를 설정하고 insert 하고 delete 한다.)
    @Test
    fun insertAndUpdateAgentTest() = runBlocking<Unit> {
        val repository by inject(AgentRepository::class.java)

        var updateAgentInfo: AgentInfo? = null

        val agentEntity = repository.findByGuid(TEST_GUID)
        withContext(Dispatchers.Main) {
            agentEntity.observeForever(agentEntityOvserver)
            agentEntity.observeForever { agentInfo ->
                updateAgentInfo = agentInfo
            }
        }

        repository.insert(createAgentInfo(TEST_GUID))
        delay(delayTime)
        MatcherAssert.assertThat("localip 가 insert 되지 않아서 실패", updateAgentInfo?.guid, Matchers.`is`(TEST_GUID))


        repository.update(AgentInfo().apply {
            guid = TEST_GUID
            localip = "localip updated"
        })
        delay(delayTime)
        MatcherAssert.assertThat("localip 가 update 되지 않아서 실패", updateAgentInfo?.localip, Matchers.`is`("localip updated"))
    }
}