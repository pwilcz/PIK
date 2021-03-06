package pik.devices.domain;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pik.devices.domain.dto.DeviceDTO;
import pik.devices.domain.dto.DeviceNotFoundException;
import pik.devices.domain.dto.VariableDTO;
import pik.devices.domain.dto.VariableNotFoundException;
import pik.devices.domain.inMemImpl.InMemoryDeviceRepository;
import pik.devices.domain.inMemImpl.InMemoryVariableRepository;
import pik.values.domain.variableModulePort.ValueVariableFacade;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static pik.devices.domain.SampleDevices.*;

public class DeviceFacadeUnitTest {

    @MockBean
    private ValueVariableFacade valueVariableFacadeMock = mock(ValueVariableFacade.class);


    private DeviceFacade deviceFacade = new DeviceConfiguration().deviceFacade(new InMemoryDeviceRepository(), new InMemoryVariableRepository(), valueVariableFacadeMock);


    @After
    public void removeDevices(){

        try {
            deviceFacade.deleteDevice(kettle.getId());
            deviceFacade.deleteDevice(washer.getId());
        } catch (DeviceNotFoundException ex) {
            //ok
        }
    }

    @Test
    public void addDevice() {
        //when
        Long id = deviceFacade.addDevice(kettle).getId();

        //then
        DeviceDTO newDevice = deviceFacade.getDevice(id);
        assertThat(newDevice.getId()).isEqualTo(kettle.getId());
        assertThat(newDevice.getName()).isEqualTo(kettle.getName());
    }

    @Test
    public void addVariable() {
        //given
        deviceFacade.addDevice(kettle);

        //when
        String id = deviceFacade.addVariable(temperature).getId();

        //then
        assertThat(deviceFacade.getVariable(id).getName()).isEqualTo(temperature.getName());
        assertThat(deviceFacade.getVariable(id).getUnit()).isEqualTo(temperature.getUnit());
        assertThat(deviceFacade.getVariable(id).getDeviceId()).isEqualTo(kettle.getId());
    }

    @Test
    public void getDevice() {
        //given
        deviceFacade.addDevice(kettle);

        //when
        DeviceDTO temp = deviceFacade.getDevice(kettle.getId());

        //then
        assertThat(temp.getId()).isEqualTo(kettle.getId());
    }

    @Test
    public void getVariable() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO dto = deviceFacade.addVariable(temperature);


        //when
        VariableDTO temp = deviceFacade.getVariable(dto.getId());

        //then
        assertThat(temp.getDeviceId()).isEqualTo(kettle.getId());
    }

    @Test(expected = DeviceNotFoundException.class)
    public void deleteDevice() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO new_temp = deviceFacade.addVariable(temperature);
        VariableDTO new_curr = deviceFacade.addVariable(current);


        //when
        deviceFacade.deleteDevice(kettle.getId());

        //then
        Mockito.verify(valueVariableFacadeMock).deleteByVariable(new_temp.getId());
        Mockito.verify(valueVariableFacadeMock).deleteByVariable(new_curr.getId());
        deviceFacade.getDevice(kettle.getId());
    }

    @Test(expected = VariableNotFoundException.class)
    public void deleteVariable() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO temp1 = deviceFacade.addVariable(temperature);
        VariableDTO temp2 = deviceFacade.addVariable(current);

        //when
        deviceFacade.deleteVariable(temp1.getId());

        //then
        assertThat(temp2.getName()).isEqualTo(current.getName());
        Mockito.verify(valueVariableFacadeMock).deleteByVariable(temp1.getId());
        deviceFacade.getVariable(temp1.getId());
    }

    @Test
    public void getDevices() {
        //given
        deviceFacade.addDevice(kettle);
        deviceFacade.addDevice(washer);

        //when
        List<DeviceDTO> devices = deviceFacade.getDevices();

        //then
        assertThat(devices.contains(kettle)).isEqualTo(true);
        assertThat(devices.contains(washer)).isEqualTo(true);
        assertThat(devices.size()).isEqualTo(2);
    }

    @Test
    public void getAllVariables() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO temp1 = deviceFacade.addVariable(temperature);
        VariableDTO temp2 = deviceFacade.addVariable(current);

        //when
        List<VariableDTO> variables = deviceFacade.getAllVariables();

        //then
        assertThat(variables.contains(temp1)).isEqualTo(true);
        assertThat(variables.contains(temp2)).isEqualTo(true);
        assertThat(variables.size()).isEqualTo(2);
    }

    @Test
    public void getVariablesByDeviceID() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO temp1 = deviceFacade.addVariable(temperature);
        VariableDTO temp2 = deviceFacade.addVariable(current);

        //when
        List<VariableDTO> variables = deviceFacade.getVariablesByDeviceID(kettle.getId());

        //then
        assertThat(variables.contains(temp1)).isEqualTo(true);
        assertThat(variables.contains(temp2)).isEqualTo(true);
    }

    @Test
    public void updateVariable() {
        //given
        deviceFacade.addDevice(kettle);
        VariableDTO temp = deviceFacade.addVariable(temperature);

        //when
        temp.setUnit("oK");
        deviceFacade.updateVariable(temp);
        VariableDTO newTemperature = deviceFacade.getVariable(temp.getId());

        //then
        assertThat(newTemperature.getUnit()).isEqualTo(temp.getUnit());
    }

    @Test
    public void updateDevice() {
        //given
        deviceFacade.addDevice(kettle);

        //when
        kettle.setName("Czajniczek");
        deviceFacade.updateDevice(kettle);
        DeviceDTO newKettle = deviceFacade.getDevice(kettle.getId());

        //then
        assertThat(newKettle.getName()).isEqualTo(kettle.getName());
    }

    @Test(expected = DeviceNotFoundException.class)
    public void whenAddingVariableToNonExistenceDeviceExceptionIsThrown() {
        //given
        deviceFacade.addDevice(kettle);

        VariableDTO fake_variable = new VariableDTO(null, "ddddd", 100987, "asa");

        //when
        deviceFacade.addVariable(fake_variable);

        //then
        //catch exception
    }


}
