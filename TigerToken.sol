pragma solidity 0.4.25;

import "github.com/OpenZeppelin/openzeppelin-solidity/contracts/token/ERC20/ERC20.sol";
import "github.com/OpenZeppelin/openzeppelin-solidity/contracts/token/ERC20/ERC20Detailed.sol";


contract TigerToken is ERC20Detailed, ERC20 {
  constructor(
    string _name,
    string _symbol,
    uint8 _decimals,
    uint256 _amount
  )
    ERC20Detailed(_name, _symbol, _decimals)
    public
  {
    require(_amount > 0, "amount has to be greater than 0");
    _mint(msg.sender, _amount.mul(10 ** uint256(_decimals)));
  }
}