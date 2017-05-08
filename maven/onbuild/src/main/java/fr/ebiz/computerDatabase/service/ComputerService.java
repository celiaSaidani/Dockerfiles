package fr.ebiz.computerDatabase.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ebiz.computerDatabase.Exception.DAOException;
import fr.ebiz.computerDatabase.Exception.ServiceException;
import fr.ebiz.computerDatabase.dto.CompanyDTO;
import fr.ebiz.computerDatabase.dto.ComputerDTO;
import fr.ebiz.computerDatabase.dto.ComputerDTOPage;
import fr.ebiz.computerDatabase.mapper.ComputerMapper;
import fr.ebiz.computerDatabase.model.Computer;
import fr.ebiz.computerDatabase.persistance.ComputerDAO;
import fr.ebiz.computerDatabase.persistance.ConnectionDB;
import fr.ebiz.computerDatabase.persistance.Transaction;
import fr.ebiz.computerDatabase.validator.ComputerValidator;
import fr.ebiz.computerDatabase.validator.DateTime;

public class ComputerService {

    private ComputerMapper computerMap;
    private ComputerDAO computerDao;
    private CompanyService companyService;
    private static final Logger logger = LoggerFactory.getLogger(ComputerService.class);
    private Connection connection = null;

    public ComputerService() {
        computerMap = new ComputerMapper();
        computerDao = new ComputerDAO();
        companyService = new CompanyService();
    }

    /**
     * updateComputer has 2 actions: insert if action equal false, update if
     * action equal false
     * @param id of computer 0 if action is an update
     * @param input is an array of attribute of a computer: name,date
     *            introduced, date disconnected,id company
     * @param action true or false
     * @return
     */
    public boolean InsertComputer(ComputerDTO comp) throws ServiceException {

        try {
            System.out.println(ComputerValidator.isValid(comp)? "dto valide":
            "No valide");
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            System.out.println("niveau sercice "+connection);
            ComputerValidator.isValid(comp);
            Computer computer;
            String name = comp.getNameComp();
            String companyId = comp.getIdCompany();
            LocalDateTime dateIn = null;
            LocalDateTime dateOut = null;

            if (comp.getDateIn() != null) {
                dateIn = DateTime.convertDate(comp.getDateIn().trim().concat(" 00:00:00"));
            }
            if (comp.getDateOut() != null) {
                dateOut = DateTime.convertDate(comp.getDateOut().trim().concat(" 00:00:00"));
            }

            if (comp.getIdCompany() != null) {
                CompanyDTO cp = companyService.getCompanybyIdLocal(Integer.parseInt(companyId));
                computer = new Computer(name, dateIn, dateOut, Integer.parseInt(cp.getIdCompany()));
            } else {
                computer = new Computer(name, dateIn, dateOut, 0);
            }
            computerDao.insert(computer);
            return true;

        } catch (DateTimeParseException | NullPointerException | DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function insert Computer");
            throw new ServiceException("can't insert computer");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }

    public Boolean updateComputer(ComputerDTO comp) throws ServiceException {
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            ComputerValidator.isValid(comp);
            Computer computer;
            int id = Integer.parseInt(comp.getIdComp());
            String name = comp.getNameComp();
            String companyId = comp.getIdCompany();
            LocalDateTime dateIn = null;
            LocalDateTime dateOut = null;

            if (comp.getDateIn() != null) {
                dateIn = DateTime.convertDate(comp.getDateIn().trim().concat(" 00:00:00"));
            }
            if (comp.getDateOut() != null) {
                dateOut = DateTime.convertDate(comp.getDateOut().trim().concat(" 00:00:00"));
            }

            if (!comp.getIdCompany().equals("")) {
                CompanyDTO cp = companyService.getCompanybyIdLocal(Integer.parseInt(companyId));
                computer = new Computer(id, name, dateIn, dateOut, Integer.parseInt(cp.getIdCompany()));
            } else
                computer = new Computer(id, name, dateIn, dateOut, 0);

            computerDao.update(computer);
            return true;

        } catch (DateTimeParseException | NullPointerException | DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function update Computer");
            throw new ServiceException("can't update computer");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }
    }

    /**
     * @param id of computer to delete
     * @return true if delete correct, false else
     */
    public boolean deleteCpmouter(int id) throws ServiceException {

        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            computerDao.delete(id);
            return true;
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function delete Computer");
            throw new ServiceException("can't delete computer");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }

    /**
     * @return list of computer
     */

    public List<ComputerDTO> getAllComputer() throws ServiceException {
        List<Computer> allComp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            allComp = computerDao.getAllComputer();
            return computerMap.getComputerDTOs(allComp);
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getAllComputer");
            throw new ServiceException("can't get all computer");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }
    }

    /**
     * @return sublist of computer
     */

    public ComputerDTOPage getAllComputerPage(int start, int end) throws ServiceException {
        ComputerDTOPage data = new ComputerDTOPage();
        List<Computer> allComp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            allComp = computerDao.getAllComputerPage(start, end);
            data.setComputersDTO(computerMap.getComputerDTOs(allComp));
            data.setCount(computerDao.CountTotalLine());
            return data;
                    //computerMap.getComputerDTOs(allComp);

        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getAllComputerPage");
            throw new ServiceException("can't get all computer by limit");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }
    }

    /**
     * @param id of computer
     * @return a computer
     */
    public ComputerDTO showDetailsComputer(int id) throws ServiceException {
        Computer cp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            cp = computerDao.getComputerById(id);
            ComputerDTO cpDto = computerMap.getComputerDTO(cp);
            if (cpDto.getDateIn() != null) {

                String newDate = cpDto.getDateIn().substring(0, 10);
                cpDto.setDateIn(newDate);
            }
            if (cpDto.getDateOut() != null) {
                String newDate = cpDto.getDateOut().substring(0, 10);
                cpDto.setDateOut(newDate);
            }
            return cpDto;
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function showDetailsComputer");
            throw new ServiceException("can't get computer Details");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }

    public List<ComputerDTO> getComputerByName(String name) throws ServiceException {
        List<Computer> cp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            cp = computerDao.getComputerByName(name);
            return computerMap.getComputerDTOs(cp);
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getComputerByName");
            throw new ServiceException("can't get list of computer by name");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }

    public List<ComputerDTO> Search(String name, int start, int end) throws ServiceException {
        List<Computer> cp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            cp = computerDao.Serach(name, start, end);
            return computerMap.getComputerDTOs(cp);

        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function search");
            throw new ServiceException("can't find list of computer");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }
    }

    public int getCount() throws ServiceException {
        try {
           
            return computerDao.CountTotalLine();
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getCount");
            throw new ServiceException("can't count computer");
        }

    }
    public int getCount(String search) throws ServiceException {
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            return computerDao.CountTotalLine(search);
        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getCount ");
            throw new ServiceException("can't count computer for on search");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }

    public List<ComputerDTO> SearchOrderBy(int start, int end, String reqorder, String reqBy, String search)
            throws ServiceException {
      
        reqorder = reqorder.trim();
        search = search.trim();

        if (reqBy.equals("up")) {
            reqBy = "ASC";
        } else {
            reqBy = "DESC";
        }
        List<Computer> lcp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            lcp = computerDao.getComputerOrderBy(start, end, reqorder, reqBy, search);
            return computerMap.getComputerDTOs(lcp);

        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getComputerOrder");
            throw new ServiceException("can't get computer orderBy");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }

    }
    public List<ComputerDTO> getComputerOrder(int start, int end, String reqBy, String name) throws ServiceException {
       
        name = name.trim();
        if (reqBy.equals("up")) {
            reqBy = "ASC";
        } else {
            reqBy = "DESC";
        }
        List<Computer> lcp;
        try {
            connection = ConnectionDB.getInstance().getConnection();
            Transaction.set(connection);
            lcp = computerDao.getComputerOrderBy(start, end, reqBy, name);
            return computerMap.getComputerDTOs(lcp);

        } catch (DAOException e) {
            System.err.println(e.getMessage());
            logger.error("[Error Service] in function getComputerOrder");
            throw new ServiceException("can't get computer orderBy");
        }
        finally {
            try {
                if (connection != null){
                    Transaction.remove();
                    connection.close();
                }

            } catch (SQLException e) {
                throw new ServiceException("enable to close connection");
            }
        }
    }


}
